package com.healthcare.patient.mapper;


import com.healthcare.patient.dto.PatientDto;
import com.healthcare.patient.model.Patient;
import org.springframework.stereotype.Component;

@Component
public class PatientMapper {

    public  Patient toEntity(PatientDto dto) {
        if (dto == null) {
            return null;
        }

        return Patient.builder()
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .dateOfBirth(dto.getDateOfBirth())
                .gender(dto.getGender())
                .phoneNumber(dto.getPhoneNumber())
                .email(dto.getEmail())
                .address(dto.getAddress())
                .emergencyContact(dto.getEmergencyContactName())
                .emergencyPhone(dto.getEmergencyContactPhone())
                .bloodType(dto.getBloodType())
                .allergies(dto.getAllergies())
                .medicalHistory(dto.getMedicalHistory())
                .build();
    }
    public PatientDto convertToDto(Patient patient) {
        PatientDto dto = new PatientDto();
        dto.setId(patient.getId());
        dto.setFirstName(patient.getFirstName());
        dto.setLastName(patient.getLastName());
        dto.setDateOfBirth(patient.getDateOfBirth());
        dto.setGender(patient.getGender());
        dto.setEmail(patient.getEmail());
        dto.setPhoneNumber(patient.getPhoneNumber());
        dto.setAddress(patient.getAddress());
        dto.setEmergencyContactName(patient.getEmergencyContact());
        dto.setEmergencyContactPhone(patient.getEmergencyPhone());
        dto.setBloodType(patient.getBloodType());
        dto.setAllergies(patient.getAllergies());
        dto.setMedicalHistory(patient.getMedicalHistory());
        dto.setStatus(patient.getStatus());
        dto.setCreatedAt(patient.getCreatedAt());
        dto.setUpdatedAt(patient.getUpdatedAt());
        dto.setAge(patient.getAge());
        return dto;
    }

    public Patient convertToEntity(PatientDto dto) {
        Patient patient = new Patient();
        patient.setFirstName(dto.getFirstName());
        patient.setLastName(dto.getLastName());
        patient.setDateOfBirth(dto.getDateOfBirth());
        patient.setGender(dto.getGender());
        patient.setEmail(dto.getEmail());
        patient.setPhoneNumber(dto.getPhoneNumber());
        patient.setAddress(dto.getAddress());
        patient.setEmergencyContact(dto.getEmergencyContactName());
        patient.setEmergencyPhone(dto.getEmergencyContactPhone());
        patient.setBloodType(dto.getBloodType());
        patient.setAllergies(dto.getAllergies());
        patient.setMedicalHistory(dto.getMedicalHistory());
        patient.setStatus(dto.getStatus() != null ? dto.getStatus() : Patient.PatientStatus.ACTIVE);
        return patient;
    }

    public void updatePatientFromDto(Patient patient, PatientDto dto) {
        if (dto.getFirstName() != null) patient.setFirstName(dto.getFirstName());
        if (dto.getLastName() != null) patient.setLastName(dto.getLastName());
        if (dto.getDateOfBirth() != null) patient.setDateOfBirth(dto.getDateOfBirth());
        if (dto.getGender() != null) patient.setGender(dto.getGender());
        if (dto.getEmail() != null) patient.setEmail(dto.getEmail());
        if (dto.getPhoneNumber() != null) patient.setPhoneNumber(dto.getPhoneNumber());
        if (dto.getAddress() != null) patient.setAddress(dto.getAddress());
        if (dto.getEmergencyContactName() != null) patient.setEmergencyContact(dto.getEmergencyContactName());
        if (dto.getEmergencyContactPhone() != null) patient.setEmergencyPhone(dto.getEmergencyContactPhone());
        if (dto.getBloodType() != null) patient.setBloodType(dto.getBloodType());
        if (dto.getAllergies() != null) patient.setAllergies(dto.getAllergies());
        if (dto.getMedicalHistory() != null) patient.setMedicalHistory(dto.getMedicalHistory());
        if (dto.getStatus() != null) patient.setStatus(dto.getStatus());
    }
}
