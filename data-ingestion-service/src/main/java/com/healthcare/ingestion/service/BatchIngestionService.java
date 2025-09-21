package com.healthcare.ingestion.service;

import com.healthcare.ingestion.dto.JobResponseDTO;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface BatchIngestionService {
    /**
     * Process patient data from a CSV file using Spring Batch
     * @param file The CSV file containing patient data
     * @return JobResponseDTO containing job execution details
     * @throws IOException if there's an error processing the file
     */
    JobResponseDTO processPatientCsv(MultipartFile file) throws IOException;

    /**
     * Process medical record data from a CSV file using Spring Batch
     * @param file The CSV file containing medical record data
     * @return JobResponseDTO containing job execution details
     * @throws IOException if there's an error processing the file
     */
    JobResponseDTO processMedicalRecordCsv(MultipartFile file) throws IOException;
}
