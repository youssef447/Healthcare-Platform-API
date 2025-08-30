package com.healthcare.ingestion.model;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class IngestionEvent {

    private String eventId;
    private String eventType;
    private String patientId;
    private String recordId;
    private String source;
    private String status;
    private String message;
    private LocalDateTime timestamp;
    private Object data;

    public enum EventType {
        PATIENT_CREATED,
        PATIENT_UPDATED,
        MEDICAL_RECORD_CREATED,
        MEDICAL_RECORD_UPDATED,
        FILE_PROCESSED,
        PROCESSING_ERROR
    }

    public enum Status {
        SUCCESS,
        FAILED,
        PROCESSING
    }

    // Constructors
    public IngestionEvent() {
        this.timestamp = LocalDateTime.now();
    }

    public IngestionEvent(String eventType, String message) {
        this();
        this.eventType = eventType;
        this.message = message;
    }

    public IngestionEvent(String eventType, String patientId, String recordId, String message) {
        this();
        this.eventType = eventType;
        this.patientId = patientId;
        this.recordId = recordId;
        this.message = message;
    }

}
