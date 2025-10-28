package com.healthcare.ingestion.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;


@Entity
@Table(name = "outbox_ingestion_event", indexes = {
        @Index(columnList = "consumed", name = "idx_consumed")
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
