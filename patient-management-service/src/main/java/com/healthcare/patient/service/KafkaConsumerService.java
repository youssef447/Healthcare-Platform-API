package com.healthcare.patient.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthcare.patient.dto.PatientDto;
import com.healthcare.patient.mapper.MedicalRecordMapper;
import com.healthcare.patient.mapper.PatientMapper;
import com.healthcare.patient.model.MedicalRecord;
import com.healthcare.patient.model.Patient;
import com.healthcare.patient.repository.MedicalRecordRepository;
import com.healthcare.patient.repository.PatientRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaConsumerService {

    private final PatientRepository patientRepository;
    private final MedicalRecordRepository medicalRecordRepository;
    private final PatientMapper patientMapper;
    private final ObjectMapper objectMapper;


    /**
     * Listens to patient-events topic and processes patient lifecycle events
     * Performs validation, caching, and analytics updates
     */
    @KafkaListener(topics = "patient-events", groupId = "patient-management-group")

    public void handlePatientEvent(Map<String, Object> event) {
        try {
            log.info("Received patient event: {}", event);


            Object payload = event.get("payload");
            if (payload instanceof String payloadString) {
                PatientDto patientDTO = objectMapper.readValue(payloadString, PatientDto.class);
                Optional<Patient> patientOpt = patientRepository.findByEmail(patientDTO.getEmail());

                if (patientOpt.isPresent()) {

                    handlePatientUpdated(patientOpt.get(), patientDTO);

                } else {
                    Patient patient = patientMapper.toEntity(patientDTO);
                    handlePatientCreated(patient);
                }
                


            } else {
                log.error("Payload is not of type PatientDTO");
            }


        } catch (Exception e) {
            log.error("Error processing patient event: {}", event, e);
        }
    }
    // ==================== Patient ====================


    private void handlePatientCreated(Patient patient) {
        log.info("Processing PATIENT_CREATED event - Patient: {}", patient);
        patientRepository.save(patient);


    }


    private void handlePatientUpdated(Patient existingPatient, PatientDto patientDto) {
        log.info("Processing PATIENT_UPDATED event - Patient: {}", existingPatient.getFullName());
        patientMapper.updatePatientFromDto(existingPatient, patientDto);
        patientRepository.save(existingPatient);


    }

    /**
     * Listens to medical-record-events topic and processes medical record events
     * Syncs data, triggers notifications, and updates patient medical history
     */
    @KafkaListener(topics = "medical-record-events", groupId = "patient-management-group")
    public void handleMedicalRecordEvent(Map<String, Object> event) {

            log.info("Received medical record event: {}", event);

            String eventType = (String) event.get("eventType");
            String patientIdStr = (String) event.get("patientId");
            String recordIdStr = (String) event.get("recordId");

            if (eventType == null || patientIdStr == null || recordIdStr == null) {
                log.warn("Invalid event: missing required fields. Event: {}", event);
                return;
            }

            try {
                Long patientId = Long.parseLong(patientIdStr);
                Long recordId = Long.parseLong(recordIdStr);


                switch (eventType.toUpperCase()) {
                    case "CREATE" -> handleMedicalRecordCreated(patientId, recordId, event);
                    case "UPDATE" -> handleMedicalRecordUpdated(patientId, recordId, event);
                    default -> log.warn("Unknown event type: {}", eventType);
                }

            } catch (Exception e) {
                log.error("Unexpected error processing medical record event: {}", event, e);
            }
    }

    private void handleMedicalRecordCreated(Long patientId, Long recordId, Map<String, Object> event) {
        log.info("Handling CREATE event for patientId={}, recordId={}", patientId, recordId);

        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new IllegalArgumentException("Patient not found with id: " + patientId));

        if (medicalRecordRepository.existsById(recordId)) {
            log.warn("Medical record already exists with ID={}, skipping creation.", recordId);
            return;
        }

        MedicalRecord record = MedicalRecordMapper.fromEventToNewRecord(patient, recordId, event);
        medicalRecordRepository.save(record);

        log.info("Medical record created successfully with ID={}", record.getId());
    }

    private void handleMedicalRecordUpdated(Long patientId, Long recordId, Map<String, Object> event) {
        log.info("Handling UPDATE event for patientId={}, recordId={}", patientId, recordId);

        MedicalRecord record = medicalRecordRepository.findById(recordId)
                .orElseThrow(() -> new IllegalArgumentException("Medical record not found with id: " + recordId));

        if (!record.getPatient().getId().equals(patientId)) {
            throw new IllegalStateException("Record does not belong to the specified patient");
        }

        MedicalRecordMapper.updateRecordFromEvent(record, event);
        medicalRecordRepository.save(record);

        log.info("Medical record updated successfully for recordId={}", recordId);
    }




}
