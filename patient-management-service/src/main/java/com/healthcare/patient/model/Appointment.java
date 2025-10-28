package com.healthcare.patient.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "appointments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Patient is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @NotBlank(message = "Doctor name is required")
    @Column(nullable = false)
    private String doctorName;

    private String department;
    private String hospitalName;

    @NotNull(message = "Appointment date is required")
    @Column(nullable = false)
    private LocalDateTime appointmentDate;

    @Builder.Default
    private Integer durationMinutes = 30;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private AppointmentStatus status = AppointmentStatus.SCHEDULED;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private AppointmentType type = AppointmentType.CONSULTATION;

    @Column(columnDefinition = "TEXT")
    private String reason;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Builder.Default
    private Boolean reminderSent = false;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public enum AppointmentStatus {
        SCHEDULED, CONFIRMED, IN_PROGRESS, COMPLETED, CANCELLED, NO_SHOW
    }

    public enum AppointmentType {
        CONSULTATION, FOLLOW_UP, EMERGENCY, SURGERY, THERAPY, CHECKUP
    }


}
