package com.healthcare.ingestion.dto;

import com.healthcare.ingestion.model.Patient;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PatientDto {
    private Long id;

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth;

    private Patient.Gender gender;

    @Email(message = "Email should be valid")
    @NotBlank
    private String email;

    private String phoneNumber;
    private String address;
    private String emergencyContact;
    private String emergencyPhone;
    private String insuranceNumber;
    private String bloodType;
    private String allergies;
    private String medicalHistory;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public String getFullName() {
        return firstName + " " + lastName;
    }
}
