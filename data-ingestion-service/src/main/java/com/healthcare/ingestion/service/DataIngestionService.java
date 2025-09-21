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

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class DataIngestionService {

    private final PatientRepository patientRepository;

    private final MedicalRecordRepository medicalRecordRepository;

    private final KafkaProducerService kafkaProducerService;

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
