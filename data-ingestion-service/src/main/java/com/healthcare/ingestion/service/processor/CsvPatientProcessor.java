package com.healthcare.ingestion.service.processor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthcare.ingestion.dto.PatientDto;
import com.healthcare.ingestion.model.Patient;
import com.healthcare.ingestion.service.KafkaProducerService;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.Reader;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class CsvPatientProcessor extends FileProcessorTemplate<PatientDto> {

    public CsvPatientProcessor(KafkaProducerService kafkaProducerService) {
        super(kafkaProducerService);
    }

    @Override
    public boolean supports(String contentType, String fileName) {
        return fileName != null && fileName.toLowerCase().endsWith(".csv");
    }

    @Override
    protected List<PatientDto> parseFile(Reader reader, String fileName) throws IOException {
        List<PatientDto> patients = new ArrayList<>();
        try (CSVReader csvReader = new CSVReader(reader)) {
            String[] headers = csvReader.readNext();
            if (headers == null) return patients;

            String[] line;
            while ((line = csvReader.readNext()) != null) {
                if (line.length >= 7) {
                    PatientDto patient = new PatientDto();
                    patient.setFirstName(line[0]);
                    patient.setLastName(line[1]);
                    patient.setDateOfBirth(parseDate(line[2]));
                    patient.setGender(Patient.Gender.valueOf(line[3].toUpperCase()));
                    patient.setPhoneNumber(line[4]);
                    patient.setEmail(line[5]);
                    patient.setAddress(line[6]);
                    patients.add(patient);
                }
            }
        } catch (CsvException e) {
            throw new IOException("Error parsing CSV file", e);
        }
        return patients;
    }

    private LocalDate parseDate(String dateStr) {
        try {
            return LocalDate.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (Exception e) {
            try {
                return LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("MM/dd/yyyy"));
            } catch (Exception e2) {
                log.warn("Unable to parse date: {}", dateStr);
                return null;
            }
        }
    }
}

