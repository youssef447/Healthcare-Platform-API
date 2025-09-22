package com.healthcare.ingestion.service;

import com.healthcare.ingestion.model.MedicalRecord;
import com.healthcare.ingestion.repository.MedicalRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MedicalRecordIngestionService {
    private final MedicalRecordRepository repo;
    private final KafkaProducerService kafka;

    public void ingestMedicalRecord(MedicalRecord record) {
        MedicalRecord saved = repo.save(record);
        kafka.publishMedicalRecordCreated(
                saved.getPatient().getId(),
                saved.getId(),
                saved.getRecordType()
        );
    }
}
