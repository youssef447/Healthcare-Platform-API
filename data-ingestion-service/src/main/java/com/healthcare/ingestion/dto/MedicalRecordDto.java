package com.healthcare.ingestion.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class MedicalRecordDto {


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
    private String status;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;


}
