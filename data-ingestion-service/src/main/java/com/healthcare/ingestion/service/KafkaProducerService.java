package com.healthcare.ingestion.service;

import com.healthcare.ingestion.entity.OutboxIngestionEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

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
     * Generic event publisher with logging and exception handling
     */
    public void publishEvent(OutboxIngestionEvent event) {
        String topic = event.getEventType() == OutboxIngestionEvent.EventType.PATIENT_CREATED ?
                PATIENT_TOPIC :
                MEDICAL_RECORD_TOPIC;
        try {
            CompletableFuture<SendResult<String, Object>> future =
                    kafkaTemplate.send(topic, event.getId().toString(), event);

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
