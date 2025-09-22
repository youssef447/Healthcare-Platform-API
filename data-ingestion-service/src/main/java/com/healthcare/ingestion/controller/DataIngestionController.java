package com.healthcare.ingestion.controller;

import com.healthcare.ingestion.config.ApiResponseBody;
import com.healthcare.ingestion.dto.JobResponseDTO;

import com.healthcare.ingestion.service.BatchIngestionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/ingestion")
@Slf4j
@Tag(name = "Data Ingestion", description = "APIs for ingesting patient and medical record data")
@RequiredArgsConstructor
public class DataIngestionController {


    private final BatchIngestionService batchIngestionService;


    @PostMapping("/patients/upload")
    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR')")
    @Operation(summary = "Upload patient data file", description = "Upload CSV file containing patient data")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "File processing started successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid file format or content"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ApiResponseBody<JobResponseDTO> uploadPatientData(
            @Parameter(description = "Patient data file (CSV)")
            @RequestParam("file") MultipartFile file) throws IOException {

        log.info("Received patient data upload request: {}", file.getOriginalFilename());


        validateFile(file);
        JobResponseDTO response = batchIngestionService.processPatientCsv(file);
        return ApiResponseBody.success(response, "File processing started successfully");


    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }
        String originalName = file.getOriginalFilename();
        if (originalName == null || !originalName.toLowerCase().endsWith(".csv")) {
            throw new IllegalArgumentException("Only CSV files are supported");
        }
    }

    @PostMapping("/medical-records/upload")
    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR')")
    @Operation(summary = "Upload medical record data file", description = "Upload CSV file containing medical record data")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "File processing started successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid file format or content"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ApiResponseBody<JobResponseDTO> uploadMedicalRecordData(
            @Parameter(description = "Medical record data file (CSV)")
            @RequestParam("file") MultipartFile file) throws IOException {

        log.info("Received medical record data upload request: {}", file.getOriginalFilename());


        validateFile(file);
        JobResponseDTO response = batchIngestionService.processMedicalRecordCsv(file);
        return ApiResponseBody.success(response, "File processing started successfully");

    }


    @GetMapping("/health")
    @Operation(summary = "Health check endpoint")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "data-ingestion-service");
        return ResponseEntity.ok(response);
    }

}
