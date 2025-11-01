package com.healthcare.patient.mapper;


import com.healthcare.patient.model.MedicalRecord;
import com.healthcare.patient.model.Patient;

import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MedicalRecordMapper {

    public static MedicalRecord fromEventToNewRecord(Patient patient, Long recordId, java.util.Map<String, Object> event) {
        return MedicalRecord.builder()
                .id(recordId)
                .patient(patient)
                .recordType(getString(event, "recordType", "General"))
                .description(getString(event, "description", ""))
                .diagnosis(getString(event, "diagnosis", null))
                .treatment(getString(event, "treatment", null))
                .medications(getString(event, "medications", null))
                .doctorName(getString(event, "doctorName", null))
                .hospitalName(getString(event, "hospitalName", null))
                .visitDate(parseDate(event.get("visitDate")))
                .followUpDate(parseDate(event.get("followUpDate")))
                .notes(getString(event, "notes", null))
                .status(MedicalRecord.RecordStatus.ACTIVE)
                .build();
    }

    public static void updateRecordFromEvent(MedicalRecord record, java.util.Map<String, Object> event) {
        if (event.containsKey("description"))
            record.setDescription(getString(event, "description", record.getDescription()));
        if (event.containsKey("diagnosis"))
            record.setDiagnosis(getString(event, "diagnosis", record.getDiagnosis()));
        if (event.containsKey("treatment"))
            record.setTreatment(getString(event, "treatment", record.getTreatment()));
        if (event.containsKey("medications"))
            record.setMedications(getString(event, "medications", record.getMedications()));
        if (event.containsKey("doctorName"))
            record.setDoctorName(getString(event, "doctorName", record.getDoctorName()));
        if (event.containsKey("hospitalName"))
            record.setHospitalName(getString(event, "hospitalName", record.getHospitalName()));
        if (event.containsKey("visitDate"))
            record.setVisitDate(parseDate(event.get("visitDate")));
        if (event.containsKey("followUpDate"))
            record.setFollowUpDate(parseDate(event.get("followUpDate")));
        if (event.containsKey("notes"))
            record.setNotes(getString(event, "notes", record.getNotes()));
    }

    private static String getString(java.util.Map<String, Object> map, String key, String defaultValue) {
        Object value = map.get(key);
        return value != null ? value.toString() : defaultValue;
    }

    private static LocalDateTime parseDate(Object dateObj) {
        if (dateObj == null) return null;
        try {
            return LocalDateTime.parse(dateObj.toString());
        } catch (Exception e) {
            log.warn("Invalid date format for field: {}", dateObj);
            return null;
        }
    }
}

