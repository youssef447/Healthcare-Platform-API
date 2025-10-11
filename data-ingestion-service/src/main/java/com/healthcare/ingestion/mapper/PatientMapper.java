package com.healthcare.ingestion.mapper;

import com.healthcare.ingestion.dto.PatientDto;
import com.healthcare.ingestion.model.Patient;
import org.springframework.stereotype.Component;

@Component
public class PatientMapper {
    public Patient toPatient(PatientDto dto) {
        return Patient.builder()
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .dateOfBirth(dto.getDateOfBirth())
                .gender(dto.getGender())
                .email(dto.getEmail())
                .phoneNumber(dto.getPhoneNumber())
                .address(dto.getAddress())
                .emergencyContact(dto.getEmergencyContact())
                .emergencyPhone(dto.getEmergencyPhone())
                .insuranceNumber(dto.getInsuranceNumber())
                .bloodType(dto.getBloodType())
                .allergies(dto.getAllergies())
                .medicalHistory(dto.getMedicalHistory())
                .build();
    }
}
