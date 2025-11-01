package com.healthcare.ingestion.batch.patient;

import com.healthcare.ingestion.dto.PatientDto;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.FieldSet;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
public class PatientFieldSetMapper implements FieldSetMapper<PatientDto> {


    @Override
    @NonNull
    public PatientDto mapFieldSet(FieldSet fieldSet) {
        PatientDto dto = new PatientDto();
        dto.setFirstName(fieldSet.readString("firstName"));
        dto.setLastName(fieldSet.readString("lastName"));

        String dob = fieldSet.readString("dateOfBirth");
        dto.setDateOfBirth(parseDate(dob));

        String gender = fieldSet.readString("gender");
        if (!gender.isBlank()) {
            try {
                dto.setGender(gender.trim().toUpperCase());
            } catch (Exception e) {
                log.warn("Invalid gender value: {}", gender);
            }
        }

        dto.setPhoneNumber(fieldSet.readString("phoneNumber"));
        dto.setEmail(fieldSet.readString("email"));
        dto.setAddress(fieldSet.readString("address"));
        dto.setEmergencyContact(fieldSet.readString("emergencyContactName"));
        dto.setEmergencyPhone(fieldSet.readString("emergencyContactNumber"));
        dto.setBloodType(fieldSet.readString("bloodType"));
        dto.setAllergies(fieldSet.readString("allergies"));
        dto.setMedicalHistory(fieldSet.readString("medicalHistory"));

        return dto;
    }

    private LocalDate parseDate(String value) {
        if (value == null || value.isBlank()) return null;

        List<DateTimeFormatter> formatters = List.of(
                DateTimeFormatter.ISO_LOCAL_DATE,
                DateTimeFormatter.ofPattern("M/d/yyyy"),
                DateTimeFormatter.ofPattern("d-M-yyyy")
        );

        for (DateTimeFormatter formatter : formatters) {
            try {
                return LocalDate.parse(value, formatter);
            } catch (Exception ignored) {

            }
        }

        log.warn("Unable to parse date: {}", value);
        return null;
    }
}
