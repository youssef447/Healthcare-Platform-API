package com.healthcare.patient.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "treatments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Treatment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Patient is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @NotBlank(message = "Treatment name is required")
    @Column(nullable = false)
    private String treatmentName;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String doctorName;
    private String hospitalName;
    private LocalDateTime startDate;
    private LocalDateTime endDate;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private TreatmentStatus status = TreatmentStatus.ACTIVE;

    @Column(columnDefinition = "TEXT")
    private String medications;

    private String dosage;
    private String frequency;

    @Column(columnDefinition = "TEXT")
    private String sideEffects;

    @Column(columnDefinition = "TEXT")
    private String notes;

    private Double cost;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public enum TreatmentStatus {
        ACTIVE, COMPLETED, CANCELLED, PAUSED
    }
}
