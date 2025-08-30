package com.healthcare.ingestion.service;

import com.healthcare.ingestion.dto.MedicalRecordDto;
import com.healthcare.ingestion.dto.PatientDto;
import com.healthcare.ingestion.service.processor.FileProcessor;
import com.healthcare.ingestion.service.processor.FileProcessorFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * Service responsible for processing different types of files (CSV, JSON) containing
 * healthcare data. Uses the Strategy pattern to delegate file processing to specific
 * processor implementations.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class FileProcessorService {

    private final FileProcessorFactory processorFactory;

    /**
     * Processes a file containing patient data.
     *
     * @param file The file to process
     * @return List of parsed PatientDto objects
     * @throws IOException if there's an error processing the file
     */
    public List<PatientDto> processPatientFile(MultipartFile file) throws IOException {
        log.info("Processing patient file: {}", file.getOriginalFilename());
        FileProcessor<PatientDto> processor = processorFactory.getProcessor(file);
        return processor.process(file);
    }

    /**
     * Processes a file containing medical record data.
     *
     * @param file The file to process
     * @return List of parsed MedicalRecordDto objects
     * @throws IOException if there's an error processing the file
     */
    public List<MedicalRecordDto> processMedicalRecordFile(MultipartFile file) throws IOException {
        log.info("Processing medical record file: {}", file.getOriginalFilename());
        FileProcessor<MedicalRecordDto> processor = processorFactory.getProcessor(file);
        return processor.process(file);
    }
}
