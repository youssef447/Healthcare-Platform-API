package com.healthcare.ingestion.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "medical_records")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MedicalRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Patient is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @NotBlank(message = "Record type is required")
    @Column(name = "record_type", nullable = false)
    private String recordType;

    @NotBlank(message = "Description is required")
    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "diagnosis", columnDefinition = "TEXT")
    private String diagnosis;

    @Column(name = "treatment", columnDefinition = "TEXT")
    private String treatment;

    @Column(name = "medications", columnDefinition = "TEXT")
    private String medications;

    @Column(name = "doctor_name")
    private String doctorName;

    @Column(name = "hospital_name")
    private String hospitalName;

    @Column(name = "visit_date")
    private LocalDateTime visitDate;

    @Column(name = "follow_up_date")
    private LocalDateTime followUpDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private RecordStatus status = RecordStatus.ACTIVE;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum RecordStatus {
        ACTIVE, INACTIVE, ARCHIVED
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Convenience constructor
    public MedicalRecord(Patient patient, String recordType, String description) {
        this.patient = patient;
        this.recordType = recordType;
        this.description = description;
    }
}
