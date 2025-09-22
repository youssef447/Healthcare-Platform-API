package com.healthcare.ingestion.mapper;

import com.healthcare.ingestion.dto.MedicalRecordDto;
import com.healthcare.ingestion.dto.PatientDto;
import com.healthcare.ingestion.model.MedicalRecord;
import com.healthcare.ingestion.model.Patient;
import lombok.experimental.UtilityClass;

@UtilityClass
public class EntityMapper {

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

    public MedicalRecord toMedicalRecord(MedicalRecordDto dto, Patient patient) {
        return MedicalRecord.builder()
                .patient(patient)
                .recordType(dto.getRecordType())
                .description(dto.getDescription())
                .diagnosis(dto.getDiagnosis())
                .treatment(dto.getTreatment())
                .medications(dto.getMedications())
                .doctorName(dto.getDoctorName())
                .hospitalName(dto.getHospitalName())
                .visitDate(dto.getVisitDate())
                .followUpDate(dto.getFollowUpDate())
                .status(dto.getStatus() != null ? dto.getStatus() : MedicalRecord.RecordStatus.ACTIVE)
                .notes(dto.getNotes())
                .build();
    }
}
