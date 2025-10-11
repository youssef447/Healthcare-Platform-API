package com.healthcare.ingestion.mapper;

import com.healthcare.ingestion.dto.MedicalRecordDto;
import com.healthcare.ingestion.dto.PatientDto;
import com.healthcare.ingestion.model.MedicalRecord;
import com.healthcare.ingestion.model.Patient;
import lombok.experimental.UtilityClass;
import org.springframework.stereotype.Component;

@Component
public class MedicalRecordMapper {


    public MedicalRecord toMedicalRecord(MedicalRecordDto dto) {
        Patient patientRef = new Patient();
        patientRef.setId(dto.getPatientId());

        return MedicalRecord.builder()
                .patient(patientRef)
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
