package com.healthcare.ingestion.service;

import com.healthcare.ingestion.model.IngestionEvent;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class KafkaProducerService {

    private static final Logger logger = LoggerFactory.getLogger(KafkaProducerService.class);

    private static final String PATIENT_TOPIC = "patient-events";
    private static final String MEDICAL_RECORD_TOPIC = "medical-record-events";
    private static final String INGESTION_TOPIC = "ingestion-events";


    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishPatientEvent(IngestionEvent event) {
        event.setEventId(UUID.randomUUID().toString());
        publishEvent(PATIENT_TOPIC, event);
    }

    public void publishMedicalRecordEvent(IngestionEvent event) {
        event.setEventId(UUID.randomUUID().toString());
        publishEvent(MEDICAL_RECORD_TOPIC, event);
    }

    public void publishIngestionEvent(IngestionEvent event) {
        event.setEventId(UUID.randomUUID().toString());
        publishEvent(INGESTION_TOPIC, event);
    }

    private void publishEvent(String topic, IngestionEvent event) {
        try {
            CompletableFuture<SendResult<String, Object>> future = 
                kafkaTemplate.send(topic, event.getEventId(), event);
            
            future.whenComplete((result, exception) -> {
                if (exception == null) {
                    logger.info("Event published successfully to topic {}: {}", 
                               topic, event.getEventId());
                } else {
                    logger.error("Failed to publish event to topic {}: {}", 
                                topic, event.getEventId(), exception);
                }
            });
        } catch (Exception e) {
            logger.error("Error publishing event to topic {}: {}", topic, event.getEventId(), e);
        }
    }

    public void publishPatientCreated(Long patientId, String patientName) {
        IngestionEvent event = new IngestionEvent(
            IngestionEvent.EventType.PATIENT_CREATED.name(),
            patientId.toString(),
            null,
            "Patient created: " + patientName
        );
        event.setStatus(IngestionEvent.Status.SUCCESS.name());
        event.setSource("data-ingestion-service");
        publishPatientEvent(event);
    }

    public void publishMedicalRecordCreated(Long patientId, Long recordId, String recordType) {
        IngestionEvent event = new IngestionEvent(
            IngestionEvent.EventType.MEDICAL_RECORD_CREATED.name(),
            patientId.toString(),
            recordId.toString(),
            "Medical record created: " + recordType
        );
        event.setStatus(IngestionEvent.Status.SUCCESS.name());
        event.setSource("data-ingestion-service");
        publishMedicalRecordEvent(event);
    }

    public void publishFileProcessed(String fileName, int recordsProcessed) {
        IngestionEvent event = new IngestionEvent(
            IngestionEvent.EventType.FILE_PROCESSED.name(),
            "File processed: " + fileName + " (" + recordsProcessed + " records)"
        );
        event.setStatus(IngestionEvent.Status.SUCCESS.name());
        event.setSource("data-ingestion-service");
        publishIngestionEvent(event);
    }

    public void publishProcessingError(String fileName, String errorMessage) {
        IngestionEvent event = new IngestionEvent(
            IngestionEvent.EventType.PROCESSING_ERROR.name(),
            "Error processing file: " + fileName + " - " + errorMessage
        );
        event.setStatus(IngestionEvent.Status.FAILED.name());
        event.setSource("data-ingestion-service");
        publishIngestionEvent(event);
    }
}
