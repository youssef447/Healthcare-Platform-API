package com.healthcare.ingestion.dto;

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

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth;

    @NotBlank(message = "Gender is required")
    private String gender;

    @Email(message = "Email should be valid")
    private String email;

    @NotBlank(message = "Phone Number is required")
    private String phoneNumber;
    @NotBlank(message = "Address is required")


    private String address;
    private String emergencyContact;
    private String emergencyPhone;
    private String insuranceNumber;
    private String bloodType;
    private String allergies;
    private String medicalHistory;


}
