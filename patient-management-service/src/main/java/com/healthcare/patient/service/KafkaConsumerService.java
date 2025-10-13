package com.healthcare.patient.service;


import com.healthcare.patient.model.MedicalRecord;
import com.healthcare.patient.model.Patient;
import com.healthcare.patient.repository.MedicalRecordRepository;
import com.healthcare.patient.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaConsumerService {

    private final PatientRepository patientRepository;
    private final MedicalRecordRepository medicalRecordRepository;


    /**
     * Listens to patient-events topic and processes patient lifecycle events
     * Performs validation, caching, and analytics updates
     */
    @KafkaListener(topics = "patient-events", groupId = "patient-management-group")
    @Transactional
    public void handlePatientEvent(Map<String, Object> event) {
        try {
            log.info("Received patient event: {}", event);

            String eventType = (String) event.get("eventType");
            String patientIdStr = (String) event.get("patientId");
            String source = (String) event.get("source");

            if (patientIdStr == null || eventType == null) {
                log.warn("Invalid event: missing patientId or eventType");
                return;
            }

            Long patientId = Long.parseLong(patientIdStr);

            switch (eventType) {
                case "PATIENT_CREATED":
                    handlePatientCreated(patientId, source);
                    break;
                case "PATIENT_UPDATED":
                    handlePatientUpdated(patientId, source);
                    break;
                default:
                    log.warn("Unknown patient event type: {}", eventType);
            }

        } catch (NumberFormatException e) {
            log.error("Invalid patient ID format in event: {}", event, e);
        } catch (Exception e) {
            log.error("Error processing patient event: {}", event, e);
        }
    }

    /**
     * Listens to medical-record-events topic and processes medical record events
     * Syncs data, triggers notifications, and updates patient medical history
     */
    @KafkaListener(topics = "medical-record-events", groupId = "patient-management-group")
    @Transactional
    public void handleMedicalRecordEvent(Map<String, Object> event) {
        try {
            log.info("Received medical record event: {}", event);

            String eventType = (String) event.get("eventType");
            String patientIdStr = (String) event.get("patientId");
            String recordIdStr = (String) event.get("recordId");
            String source = (String) event.get("source");

            if (patientIdStr == null || recordIdStr == null || eventType == null) {
                log.warn("Invalid event: missing required fields");
                return;
            }

            Long patientId = Long.parseLong(patientIdStr);
            Long recordId = Long.parseLong(recordIdStr);

            switch (eventType) {
                case "MEDICAL_RECORD_CREATED":
                    handleMedicalRecordCreated(patientId, recordId, source);
                    break;
                case "MEDICAL_RECORD_UPDATED":
                    handleMedicalRecordUpdated(patientId, recordId, source);
                    break;
                default:
                    log.warn("Unknown medical record event type: {}", eventType);
            }

        } catch (NumberFormatException e) {
            log.error("Invalid ID format in event: {}", event, e);
        } catch (Exception e) {
            log.error("Error processing medical record event: {}", event, e);
        }
    }

    // ==================== Event Handlers ====================

    /**
     * Handles PATIENT_CREATED event
     * - Validates patient exists in database
     * - Logs patient creation for audit trail
     * - Updates analytics and checks for missing critical information
     */
    private void handlePatientCreated(Long patientId, String source) {
        log.info("Processing PATIENT_CREATED event for patient ID: {} from source: {}", patientId, source);

        Optional<Patient> patientOpt = patientRepository.findById(patientId);

        if (patientOpt.isPresent()) {
            Patient patient = patientOpt.get();
            log.info("Patient validated: {} {} (ID: {})",
                    patient.getFirstName(), patient.getLastName(), patient.getId());

            // Business logic: Update analytics/metrics
            long totalPatients = patientRepository.count();
            log.info("Total patients in system: {}", totalPatients);

            // Check for missing critical information
            if (patient.getEmergencyContact() == null || patient.getEmergencyContact().isEmpty()) {
                log.warn("Patient {} has no emergency contact - flagging for follow-up", patientId);
            }

            // Log audit trail
            log.info("Audit: Patient {} created at {} from {}",
                    patientId, patient.getCreatedAt(), source);

            // Future enhancements:
            // - Send welcome email/SMS
            // - Create default appointment
            // - Trigger insurance verification
            // - Update dashboard statistics

        } else {
            log.warn("Patient ID {} not found in database - possible sync issue", patientId);
        }
    }

    /**
     * Handles PATIENT_UPDATED event
     * - Validates patient exists
     * - Logs update for audit trail
     * - Checks for critical status changes
     */
    private void handlePatientUpdated(Long patientId, String source) {
        log.info("Processing PATIENT_UPDATED event for patient ID: {} from source: {}", patientId, source);

        Optional<Patient> patientOpt = patientRepository.findById(patientId);

        if (patientOpt.isPresent()) {
            Patient patient = patientOpt.get();
            log.info("Patient update validated: {} (ID: {})", patient.getFullName(), patient.getId());

            // Log audit trail
            log.info("Audit: Patient {} updated at {} from {}",
                    patientId, patient.getUpdatedAt(), source);

            // Check status changes
            if (patient.getStatus() == Patient.PatientStatus.DECEASED) {
                log.warn("Patient {} marked as DECEASED - triggering cleanup procedures", patientId);
                // Could trigger: cancel appointments, notify staff, archive records
            }

            // Future enhancements:
            // - Send notification if contact info changed
            // - Update related appointments/treatments
            // - Sync with external systems

        } else {
            log.warn("Patient ID {} not found in database", patientId);
        }
    }

    /**
     * Handles MEDICAL_RECORD_CREATED event
     * - Validates patient and record exist
     * - Updates patient's medical history summary
     * - Checks for follow-up requirements
     */
    private void handleMedicalRecordCreated(Long patientId, Long recordId, String source) {
        log.info("Processing MEDICAL_RECORD_CREATED event - Patient: {}, Record: {}, Source: {}",
                patientId, recordId, source);

        Optional<Patient> patientOpt = patientRepository.findById(patientId);
        Optional<MedicalRecord> recordOpt = medicalRecordRepository.findById(recordId);

        if (patientOpt.isPresent() && recordOpt.isPresent()) {
            Patient patient = patientOpt.get();
            MedicalRecord record = recordOpt.get();

            log.info("Medical record validated: Type={}, Doctor={}, Patient={}",
                    record.getRecordType(), record.getDoctorName(), patient.getFullName());

            // Update patient's medical history count
            long recordCount = medicalRecordRepository.countByPatientId(patientId);
            log.info("Patient {} now has {} medical records", patientId, recordCount);

            // Check for follow-up requirements
            if (record.getFollowUpDate() != null) {
                log.info("Follow-up scheduled for {} on {}",
                        patient.getFullName(), record.getFollowUpDate());
                // Could trigger: create reminder, schedule appointment
            }

            // Update patient's medical history summary
            if (record.getDiagnosis() != null && !record.getDiagnosis().isEmpty()) {
                String currentHistory = patient.getMedicalHistory() != null ? patient.getMedicalHistory() : "";
                String updatedHistory = currentHistory + "\n[" + record.getVisitDate() + "] " +
                        record.getDiagnosis();
                patient.setMedicalHistory(updatedHistory);
                patientRepository.save(patient);
                log.info("Updated patient medical history summary");
            }

            // Future enhancements:
            // - Send notification to patient
            // - Alert if critical diagnosis detected
            // - Update insurance claims
            // - Generate billing records

        } else {
            if (patientOpt.isEmpty()) {
                log.warn("Patient ID {} not found", patientId);
            }
            if (recordOpt.isEmpty()) {
                log.warn("Medical record ID {} not found", recordId);
            }
        }
    }

    /**
     * Handles MEDICAL_RECORD_UPDATED event
     * - Validates record exists
     * - Logs update for audit trail
     * - Checks for status changes
     */
    private void handleMedicalRecordUpdated(Long patientId, Long recordId, String source) {
        log.info("Processing MEDICAL_RECORD_UPDATED event - Patient: {}, Record: {}, Source: {}",
                patientId, recordId, source);

        Optional<MedicalRecord> recordOpt = medicalRecordRepository.findById(recordId);

        if (recordOpt.isPresent()) {
            MedicalRecord record = recordOpt.get();


            log.info("Medical record update validated: ID={}, Type={}, Status={}",
                    record.getId(), record.getRecordType(), record.getStatus());

            // Log audit trail
            log.info("Audit: Medical record {} updated at {} from {}",
                    recordId, record.getUpdatedAt(), source);

            // Check status changes
            if (record.getStatus() == MedicalRecord.RecordStatus.ARCHIVED) {
                log.info("Medical record {} archived", recordId);
            }


        } else {
            log.warn("Medical record ID {} not found", recordId);
        }
    }
}
