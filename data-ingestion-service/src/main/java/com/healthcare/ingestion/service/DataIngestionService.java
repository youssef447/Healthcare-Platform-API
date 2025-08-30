package com.healthcare.ingestion.service;

import com.healthcare.ingestion.dto.MedicalRecordDto;
import com.healthcare.ingestion.dto.PatientDto;
import com.healthcare.ingestion.mapper.EntityMapper;
import com.healthcare.ingestion.model.MedicalRecord;
import com.healthcare.ingestion.model.Patient;
import com.healthcare.ingestion.repository.MedicalRecordRepository;
import com.healthcare.ingestion.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class DataIngestionService {



    private final PatientRepository patientRepository;


    private final MedicalRecordRepository medicalRecordRepository;


    private final FileProcessorService fileProcessorService;


    private final KafkaProducerService kafkaProducerService;

    public List<Patient> ingestPatientData(MultipartFile file) throws IOException {
        log.info("Starting patient data ingestion from file: {}", file.getOriginalFilename());
        
        List<PatientDto> patientDtos = fileProcessorService.processPatientFile(file);
        List<Patient> savedPatients = new ArrayList<>();

        for (PatientDto patientDto : patientDtos) {
            try {
                Patient patient = convertToPatientEntity(patientDto);
                
                // Check if patient already exists by email
                Optional<Patient> existingPatient = patientRepository.findByEmail(patient.getEmail());
                if (existingPatient.isPresent()) {
                    log.warn("Patient with email {} already exists, skipping", patient.getEmail());
                    continue;
                }

                Patient savedPatient = patientRepository.save(patient);
                savedPatients.add(savedPatient);
                
                // Publish Kafka event
                kafkaProducerService.publishPatientCreated(
                    savedPatient.getId(), 
                    savedPatient.getFullName()
                );
                
                log.debug("Successfully saved patient: {}", savedPatient.getFullName());
                
            } catch (Exception e) {
                log.error("Error saving patient: {}", patientDto.getFullName(), e);
            }
        }

        log.info("Successfully ingested {} patients from file: {}",
                   savedPatients.size(), file.getOriginalFilename());
        return savedPatients;
    }

    public List<MedicalRecord> ingestMedicalRecordData(MultipartFile file) throws IOException {
        log.info("Starting medical record data ingestion from file: {}", file.getOriginalFilename());
        
        List<MedicalRecordDto> recordDtos = fileProcessorService.processMedicalRecordFile(file);
        List<MedicalRecord> savedRecords = new ArrayList<>();

        for (MedicalRecordDto recordDto : recordDtos) {
            try {
                // Verify patient exists
                Optional<Patient> patient = patientRepository.findById(recordDto.getPatientId());
                if (patient.isEmpty()) {
                    log.warn("Patient with ID {} not found, skipping medical record", recordDto.getPatientId());
                    continue;
                }

                MedicalRecord record = convertToMedicalRecordEntity(recordDto, patient.get());
                MedicalRecord savedRecord = medicalRecordRepository.save(record);
                savedRecords.add(savedRecord);
                
                // Publish Kafka event
                kafkaProducerService.publishMedicalRecordCreated(
                    savedRecord.getPatient().getId(),
                    savedRecord.getId(),
                    savedRecord.getRecordType()
                );
                
                log.debug("Successfully saved medical record for patient: {}",
                           patient.get().getFullName());
                
            } catch (Exception e) {
                log.error("Error saving medical record for patient ID: {}",
                           recordDto.getPatientId(), e);
            }
        }

        log.info("Successfully ingested {} medical records from file: {}",
                   savedRecords.size(), file.getOriginalFilename());
        return savedRecords;
    }

    public Patient createPatient(PatientDto patientDto) {
        log.info("Creating new patient: {}", patientDto.getFullName());
        
        // Check if patient already exists by email
        if (patientRepository.existsByEmail(patientDto.getEmail())) {
            throw new IllegalArgumentException("Patient with email " + patientDto.getEmail() + " already exists");
        }

        Patient patient = convertToPatientEntity(patientDto);
        Patient savedPatient = patientRepository.save(patient);
        
        // Publish Kafka event
        kafkaProducerService.publishPatientCreated(
            savedPatient.getId(), 
            savedPatient.getFullName()
        );
        
        log.info("Successfully created patient: {}", savedPatient.getFullName());
        return savedPatient;
    }

    public MedicalRecord createMedicalRecord(MedicalRecordDto recordDto) {
        log.info("Creating new medical record for patient ID: {}", recordDto.getPatientId());
        
        Optional<Patient> patient = patientRepository.findById(recordDto.getPatientId());
        if (patient.isEmpty()) {
            throw new IllegalArgumentException("Patient with ID " + recordDto.getPatientId() + " not found");
        }

        MedicalRecord record = convertToMedicalRecordEntity(recordDto, patient.get());
        MedicalRecord savedRecord = medicalRecordRepository.save(record);
        
        // Publish Kafka event
        kafkaProducerService.publishMedicalRecordCreated(
            savedRecord.getPatient().getId(),
            savedRecord.getId(),
            savedRecord.getRecordType()
        );
        
        log.info("Successfully created medical record for patient: {}",
                   patient.get().getFullName());
        return savedRecord;
    }

    private Patient convertToPatientEntity(PatientDto dto) {
        return EntityMapper.toPatient(dto);
    }

    private MedicalRecord convertToMedicalRecordEntity(MedicalRecordDto dto, Patient patient) {
        return EntityMapper.toMedicalRecord(dto, patient);
    }
}
