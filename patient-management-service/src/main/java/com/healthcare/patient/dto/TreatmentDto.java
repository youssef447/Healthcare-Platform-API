package com.healthcare.patient.dto;

import com.healthcare.patient.model.Treatment;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TreatmentDto {
    private Long id;

    @NotNull(message = "Patient ID is required")
    private Long patientId;

    @NotBlank(message = "Treatment name is required")
    private String treatmentName;

    private String description;
    private String doctorName;
    private String hospitalName;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Treatment.TreatmentStatus status;
    private String medications;
    private String dosage;
    private String frequency;
    private String sideEffects;
    private String notes;
    private Double cost;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
