package com.healthcare.ingestion.service.processor;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthcare.ingestion.dto.PatientDto;
import com.healthcare.ingestion.service.KafkaProducerService;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.Reader;
import java.util.List;

@Component
public class JsonPatientProcessor extends FileProcessorTemplate<PatientDto> {
    private final ObjectMapper objectMapper;

    public JsonPatientProcessor(KafkaProducerService kafkaProducerService, ObjectMapper objectMapper) {
        super(kafkaProducerService);
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean supports(String contentType, String fileName) {
        return fileName != null && fileName.toLowerCase().endsWith(".json");
    }

    @Override
    protected List<PatientDto> parseFile(Reader reader, String fileName) throws IOException {
        return objectMapper.readValue(reader, new TypeReference<>() {
        });
    }
}

