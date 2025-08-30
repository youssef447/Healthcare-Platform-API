package com.healthcare.ingestion.controller;

import com.healthcare.ingestion.dto.MedicalRecordDto;
import com.healthcare.ingestion.dto.PatientDto;
import com.healthcare.ingestion.model.MedicalRecord;
import com.healthcare.ingestion.model.Patient;
import com.healthcare.ingestion.service.DataIngestionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ingestion")
@Slf4j
@Tag(name = "Data Ingestion", description = "APIs for ingesting patient and medical record data")
@RequiredArgsConstructor
public class DataIngestionController {


    private final DataIngestionService dataIngestionService;

    @GetMapping("/health")
    @Operation(summary = "Health check endpoint")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "data-ingestion-service");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/patients/upload")
    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR')")
    @Operation(summary = "Upload patient data file", description = "Upload CSV or JSON file containing patient data")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "File processed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid file format or content"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Map<String, Object>> uploadPatientData(
            @Parameter(description = "Patient data file (CSV or JSON)")
            @RequestParam("file") MultipartFile file) {

        log.info("Received patient data upload request: {}", file.getOriginalFilename());

        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("File is empty"));
            }

            List<Patient> patients = dataIngestionService.ingestPatientData(file);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Patient data uploaded successfully");
            response.put("fileName", file.getOriginalFilename());
            response.put("patientsProcessed", patients.size());
            response.put("status", "SUCCESS");

            return ResponseEntity.ok(response);

        } catch (IOException e) {
            log.error("Error processing patient file: {}", file.getOriginalFilename(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error processing file: " + e.getMessage()));
        } catch (IllegalArgumentException e) {
            log.error("Invalid file format: {}", file.getOriginalFilename(), e);
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("Invalid file format: " + e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error processing patient file: {}", file.getOriginalFilename(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Unexpected error: " + e.getMessage()));
        }
    }

    @PostMapping("/medical-records/upload")
    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR')")
    @Operation(summary = "Upload medical record data file", description = "Upload CSV or JSON file containing medical record data")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "File processed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid file format or content"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Map<String, Object>> uploadMedicalRecordData(
            @Parameter(description = "Medical record data file (CSV or JSON)")
            @RequestParam("file") MultipartFile file) {

        log.info("Received medical record data upload request: {}", file.getOriginalFilename());

        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("File is empty"));
            }

            List<MedicalRecord> records = dataIngestionService.ingestMedicalRecordData(file);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Medical record data uploaded successfully");
            response.put("fileName", file.getOriginalFilename());
            response.put("recordsProcessed", records.size());
            response.put("status", "SUCCESS");

            return ResponseEntity.ok(response);

        } catch (IOException e) {
            log.error("Error processing medical record file: {}", file.getOriginalFilename(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error processing file: " + e.getMessage()));
        } catch (IllegalArgumentException e) {
            log.error("Invalid file format: {}", file.getOriginalFilename(), e);
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("Invalid file format: " + e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error processing medical record file: {}", file.getOriginalFilename(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Unexpected error: " + e.getMessage()));
        }
    }

    @PostMapping("/patients")
    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR')")
    @Operation(summary = "Create a new patient", description = "Create a single patient record")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Patient created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid patient data"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "409", description = "Patient already exists")
    })
    public ResponseEntity<Map<String, Object>> createPatient(
            @Valid @RequestBody PatientDto patientDto) {

        log.info("Received create patient request: {}", patientDto.getFullName());

        try {
            Patient patient = dataIngestionService.createPatient(patientDto);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Patient created successfully");
            response.put("patientId", patient.getId());
            response.put("patientName", patient.getFullName());
            response.put("status", "SUCCESS");

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalArgumentException e) {
            log.error("Patient already exists: {}", patientDto.getEmail(), e);
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(createErrorResponse("Patient already exists: " + e.getMessage()));
        } catch (Exception e) {
            log.error("Error creating patient: {}", patientDto.getFullName(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error creating patient: " + e.getMessage()));
        }
    }

    @PostMapping("/medical-records")
    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR')")
    @Operation(summary = "Create a new medical record", description = "Create a single medical record")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Medical record created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid medical record data"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Patient not found")
    })
    public ResponseEntity<Map<String, Object>> createMedicalRecord(
            @Valid @RequestBody MedicalRecordDto recordDto) {

        log.info("Received create medical record request for patient ID: {}", recordDto.getPatientId());

        try {
            MedicalRecord record = dataIngestionService.createMedicalRecord(recordDto);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Medical record created successfully");
            response.put("recordId", record.getId());
            response.put("patientId", record.getPatient().getId());
            response.put("recordType", record.getRecordType());
            response.put("status", "SUCCESS");

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalArgumentException e) {
            log.error("Patient not found for medical record: {}", recordDto.getPatientId(), e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse("Patient not found: " + e.getMessage()));
        } catch (Exception e) {
            log.error("Error creating medical record for patient ID: {}", recordDto.getPatientId(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error creating medical record: " + e.getMessage()));
        }
    }

    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "ERROR");
        response.put("message", message);
        response.put("timestamp", System.currentTimeMillis());
        return response;
    }
}
