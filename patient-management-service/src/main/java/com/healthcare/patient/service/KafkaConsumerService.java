package com.healthcare.patient.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaConsumerService {



    @KafkaListener(topics = "patient-events", groupId = "patient-management-group")
    public void handlePatientEvent(Map<String, Object> event) {
        try {
            log.info("Received patient event: {}", event);
            
            String eventType = (String) event.get("eventType");
            String patientId = (String) event.get("patientId");
            String message = (String) event.get("message");
            
            switch (eventType) {
                case "PATIENT_CREATED":
                    log.info("Patient created event received for patient ID: {}", patientId);
                    // Additional processing if needed
                    break;
                case "PATIENT_UPDATED":
                    log.info("Patient updated event received for patient ID: {}", patientId);
                    // Additional processing if needed
                    break;
                default:
                    log.warn("Unknown patient event type: {}", eventType);
            }
            
        } catch (Exception e) {
            log.error("Error processing patient event: {}", event, e);
        }
    }

    @KafkaListener(topics = "medical-record-events", groupId = "patient-management-group")
    public void handleMedicalRecordEvent(Map<String, Object> event) {
        try {
            log.info("Received medical record event: {}", event);
            
            String eventType = (String) event.get("eventType");
            String patientId = (String) event.get("patientId");
            String recordId = (String) event.get("recordId");
            String message = (String) event.get("message");
            
            switch (eventType) {
                case "MEDICAL_RECORD_CREATED":
                    log.info("Medical record created event received for patient ID: {}, record ID: {}",
                               patientId, recordId);
                    // Additional processing if needed
                    break;
                case "MEDICAL_RECORD_UPDATED":
                    log.info("Medical record updated event received for patient ID: {}, record ID: {}",
                               patientId, recordId);
                    // Additional processing if needed
                    break;
                default:
                    log.warn("Unknown medical record event type: {}", eventType);
            }
            
        } catch (Exception e) {
            log.error("Error processing medical record event: {}", event, e);
        }
    }

    @KafkaListener(topics = "ingestion-events", groupId = "patient-management-group")
    public void handleIngestionEvent(Map<String, Object> event) {
        try {
            log.info("Received ingestion event: {}", event);
            
            String eventType = (String) event.get("eventType");
            String message = (String) event.get("message");
            String status = (String) event.get("status");
            
            switch (eventType) {
                case "FILE_PROCESSED":
                    log.info("File processing event received: {}", message);
                    // Additional processing if needed
                    break;
                case "PROCESSING_ERROR":
                    log.error("Processing error event received: {}", message);
                    // Additional error handling if needed
                    break;
                default:
                    log.warn("Unknown ingestion event type: {}", eventType);
            }
            
        } catch (Exception e) {
            log.error("Error processing ingestion event: {}", event, e);
        }
    }
}
