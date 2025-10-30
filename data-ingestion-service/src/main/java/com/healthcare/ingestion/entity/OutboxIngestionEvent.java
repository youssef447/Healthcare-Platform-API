package com.healthcare.ingestion.entity;

import com.healthcare.ingestion.converter.JsonAttributeConverter;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "outbox_ingestion_event", indexes = {
        @Index(columnList = "created_at", name = "idx_created_at"),
        @Index(columnList = "status", name = "idx_status")
})
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class OutboxIngestionEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Enumerated(EnumType.STRING)
    private EventType eventType = EventType.PATIENT_CREATED;
    @Enumerated(EnumType.STRING)
    private Status status = Status.PENDING;
    private String source;
    
    @Convert(converter = JsonAttributeConverter.class)
    @Column(columnDefinition = "TEXT")
    private Object payload;

    public enum EventType {
        PATIENT_CREATED,
        MEDICAL_RECORD_CREATED

    }

    LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();

    }


    public enum Status {
        PENDING,
        PROCESSING,
        COMPLETED

    }
}
