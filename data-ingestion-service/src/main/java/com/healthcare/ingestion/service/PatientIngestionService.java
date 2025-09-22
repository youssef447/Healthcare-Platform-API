package com.healthcare.ingestion.service;

import com.healthcare.ingestion.model.Patient;
import com.healthcare.ingestion.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PatientIngestionService {
    private final PatientRepository patientRepository;
    private final KafkaProducerService kafkaProducerService;

    public void ingestPatient(Patient patient) {
        if (patient.getEmail() == null || patient.getEmail().isBlank()) {
            throw new IllegalArgumentException("Missing email for patient: " + patient.getFullName());

        }
        if (patientRepository.existsByEmail(patient.getEmail())) {
            throw new IllegalStateException("Patient with email " + patient.getEmail() + " already exists");
        }

        Patient saved = patientRepository.save(patient);
        kafkaProducerService.publishPatientCreated(saved.getId(), saved.getFullName());
    }
}
