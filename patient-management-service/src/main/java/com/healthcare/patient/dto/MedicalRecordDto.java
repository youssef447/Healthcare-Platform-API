package com.healthcare.patient.dto;

import com.healthcare.patient.model.MedicalRecord;
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
public class MedicalRecordDto {
    private Long id;

    @NotNull(message = "Patient ID is required")
    private Long patientId;

    @NotBlank(message = "Record type is required")
    private String recordType;

    @NotBlank(message = "Description is required")
    private String description;

    private String diagnosis;
    private String treatment;
    private String medications;
    private String doctorName;
    private String hospitalName;
    private LocalDateTime visitDate;
    private LocalDateTime followUpDate;
    private MedicalRecord.RecordStatus status;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
