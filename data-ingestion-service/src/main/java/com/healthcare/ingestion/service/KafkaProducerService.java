package com.healthcare.ingestion.service;

import com.healthcare.ingestion.model.IngestionEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaProducerService {

    private static final String PATIENT_TOPIC = "patient-events";
    private static final String MEDICAL_RECORD_TOPIC = "medical-record-events";
    private static final String INGESTION_SOURCE = "data-ingestion-service";

    private final KafkaTemplate<String, Object> kafkaTemplate;


    /**
     * Helper to publish 'Patient Created' event
     */
    public void publishPatientCreated(Long patientId) {
        IngestionEvent event = IngestionEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType(IngestionEvent.EventType.PATIENT_CREATED)
                .patientId(patientId.toString())
                .source(INGESTION_SOURCE)
                .build();

        publishEvent(PATIENT_TOPIC, event);
    }


    /**
     * Helper to publish 'Medical Record Created' event
     */
    public void publishMedicalRecordCreated(Long patientId, Long recordId) {
        IngestionEvent event = IngestionEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType(IngestionEvent.EventType.MEDICAL_RECORD_CREATED)
                .patientId(patientId.toString())
                .recordId(recordId.toString())
                .source(INGESTION_SOURCE)

                .build();
        publishEvent(MEDICAL_RECORD_TOPIC, event);
    }

    /**
     * Generic event publisher with logging and exception handling
     */
    private void publishEvent(String topic, IngestionEvent event) {
        try {
            CompletableFuture<SendResult<String, Object>> future =
                    kafkaTemplate.send(topic, event.getEventId(), event);

            future.whenComplete((result, exception) -> {
                if (exception == null) {
                    log.info("Event [{}] published successfully to topic [{}]", event.getEventType(), topic);
                } else {
                    log.error("Failed to publish event [{}] to topic [{}]", event.getEventType(), topic, exception);
                }
            });
        } catch (Exception e) {
            log.error("Error publishing event [{}] to topic [{}]", event.getEventType(), topic, e);
        }
    }


}
