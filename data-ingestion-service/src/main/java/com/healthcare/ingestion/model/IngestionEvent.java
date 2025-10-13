package com.healthcare.ingestion.model;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IngestionEvent {

    private String eventId;
    private EventType eventType;
    private String patientId;
    private String recordId;
    private String source;
    private Object payload;

    public enum EventType {
        PATIENT_CREATED,
        PATIENT_UPDATED,
        MEDICAL_RECORD_CREATED,
        MEDICAL_RECORD_UPDATED,

    }


    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
