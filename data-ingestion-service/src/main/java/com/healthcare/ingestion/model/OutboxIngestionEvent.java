package com.healthcare.ingestion.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;


@Entity
@Table(indexes = {
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
    private String source;
    private Object payload;
    private boolean consumed = false;

    public enum EventType {
        PATIENT_CREATED,
        MEDICAL_RECORD_CREATED

    }

    LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();

    }
}
