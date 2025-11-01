package com.healthcare.ingestion.batch.medical_record;

import com.healthcare.ingestion.dto.MedicalRecordDto;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.FieldSet;


import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
public class MedicalRecordFieldSetMapper implements FieldSetMapper<MedicalRecordDto> {


    @Override
    @NonNull
    public MedicalRecordDto mapFieldSet(@NonNull FieldSet fieldSet) {
        MedicalRecordDto dto = new MedicalRecordDto();
        dto.setPatientId(readLong(fieldSet, "patientId"));
        dto.setRecordType(fieldSet.readString("recordType"));
        dto.setDescription(fieldSet.readString("description"));
        dto.setDiagnosis(fieldSet.readString("diagnosis"));
        dto.setTreatment(fieldSet.readString("treatment"));
        dto.setMedications(fieldSet.readString("medications"));
        dto.setDoctorName(fieldSet.readString("doctorName"));
        dto.setHospitalName(fieldSet.readString("hospitalName"));
        dto.setVisitDate(parseDateTime(fieldSet.readString("visitDate")));
        dto.setFollowUpDate(parseDateTime(fieldSet.readString("followUpDate")));

        String status = fieldSet.readString("status");
        if (!status.isBlank()) {
            try {
                dto.setStatus(status.trim().toUpperCase());
            } catch (Exception e) {
                log.warn("Invalid record status: {}", status);
            }
        }

        dto.setNotes(fieldSet.readString("notes"));
        return dto;
    }

    private Long readLong(FieldSet fs, String name) {
        try {
            return fs.readLong(name);
        } catch (Exception e) {
            return null;
        }
    }

    private LocalDateTime parseDateTime(String value) {
        if (value == null || value.isBlank()) return null;

        List<DateTimeFormatter> fmts = List.of(
                DateTimeFormatter.ISO_LOCAL_DATE_TIME,
                DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm"),
                DateTimeFormatter.ofPattern("MM/dd/yyyy")
        );

        for (DateTimeFormatter f : fmts) {
            try {
                return LocalDateTime.parse(value, f);
            } catch (Exception ignored) {
            }
        }

        log.warn("Unable to parse dateTime: {}", value);
        return null;
    }
}
