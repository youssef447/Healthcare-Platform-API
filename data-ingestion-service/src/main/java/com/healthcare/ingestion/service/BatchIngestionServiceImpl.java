package com.healthcare.ingestion.service;

import com.healthcare.ingestion.dto.JobResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
@Service
@RequiredArgsConstructor
public class BatchIngestionServiceImpl implements BatchIngestionService {

    private final JobLauncher jobLauncher;
    private final Job patientCsvJob;
    private final Job medicalRecordCsvJob;

    @Override
    public JobResponseDTO processPatientCsv(MultipartFile file) throws IOException {
        try {

            log.info("Processing patient CSV file: {}", file.getOriginalFilename());
            Path tempFilePath = saveToTempFile(file, "patients_upload_");
            var execution = jobLauncher.run(patientCsvJob, createJobParameters(tempFilePath));

            return JobResponseDTO.builder().message(
                    "Patient CSV accepted for processing").fileName(
                    file.getOriginalFilename()).jobExecutionId(
                    execution.getId()).build();

        } catch (Exception e) {
            log.error("Error processing patient CSV file: {}", file.getOriginalFilename(), e);
            throw new IOException("Failed to process patient CSV file: " + e.getMessage(), e);
        }


    }


    @Override
    public JobResponseDTO processMedicalRecordCsv(MultipartFile file) throws IOException {
        try {
            log.info("Processing medical record CSV file: {}", file.getOriginalFilename());
            Path tempFile = saveToTempFile(file, "medical_records_upload_");
            var execution = jobLauncher.run(medicalRecordCsvJob, createJobParameters(tempFile));

            return JobResponseDTO.builder().message(
                    "Medical record CSV accepted for processing").fileName(
                    file.getOriginalFilename()).jobExecutionId(
                    execution.getId()).build();
        } catch (Exception e) {
            log.error("Error processing medical record CSV file: {}", file.getOriginalFilename(), e);
            throw new IOException("Failed to process medical record CSV file: " + e.getMessage(), e);
        }
    }

    private JobParameters createJobParameters(Path tempFile) {
        return new JobParametersBuilder()
                .addString("filePath", tempFile.toAbsolutePath().toString())
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters();
    }

    private Path saveToTempFile(MultipartFile multipartFile, String prefix) throws IOException {
        String originalName = multipartFile.getOriginalFilename();
        String ext = (originalName != null && originalName.contains(".")) ?
                originalName.substring(originalName.lastIndexOf('.')) : ".csv";
        Path temp = Files.createTempFile(prefix, ext);
        Files.write(temp, multipartFile.getBytes());
        return temp;
    }
}
