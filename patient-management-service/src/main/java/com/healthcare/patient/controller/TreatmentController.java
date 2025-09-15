package com.healthcare.patient.controller;

import com.healthcare.patient.dto.TreatmentDto;
import com.healthcare.patient.model.Treatment;
import com.healthcare.patient.service.TreatmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/treatments")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Treatment Management", description = "APIs for managing patient treatments")
public class TreatmentController {


    
    private final TreatmentService treatmentService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR') or hasRole('NURSE')")
    @Operation(summary = "Get all treatments", description = "Retrieve all treatments with optional pagination")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Treatments retrieved successfully"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<Page<TreatmentDto>> getAllTreatments(Pageable pageable) {
        log.info("Received request to get all treatments with pagination");
        Page<TreatmentDto> treatments = treatmentService.getAllTreatments(pageable);
        return ResponseEntity.ok(treatments);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR') or hasRole('NURSE') or (hasRole('PATIENT') and @treatmentService.isPatientOwner(#id, authentication.name))")
    @Operation(summary = "Get treatment by ID", description = "Retrieve a specific treatment by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Treatment found"),
        @ApiResponse(responseCode = "404", description = "Treatment not found"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<TreatmentDto> getTreatmentById(
            @Parameter(description = "Treatment ID") @PathVariable Long id) {
        log.info("Received request to get treatment by ID: {}", id);
        
        Optional<TreatmentDto> treatment = treatmentService.getTreatmentById(id);
        return treatment.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR') or hasRole('NURSE') or (hasRole('PATIENT') and @patientService.isPatientOwner(#patientId, authentication.name))")
    @Operation(summary = "Get treatments by patient ID", description = "Retrieve all treatments for a specific patient")
    public ResponseEntity<List<TreatmentDto>> getTreatmentsByPatientId(
            @Parameter(description = "Patient ID") @PathVariable Long patientId) {
        log.info("Received request to get treatments for patient ID: {}", patientId);
        List<TreatmentDto> treatments = treatmentService.getTreatmentsByPatientId(patientId);
        return ResponseEntity.ok(treatments);
    }

    @GetMapping("/patient/{patientId}/status/{status}")
    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR') or hasRole('NURSE') or (hasRole('PATIENT') and @patientService.isPatientOwner(#patientId, authentication.name))")
    @Operation(summary = "Get treatments by patient and status", description = "Retrieve treatments for a patient filtered by status")
    public ResponseEntity<List<TreatmentDto>> getTreatmentsByPatientIdAndStatus(
            @Parameter(description = "Patient ID") @PathVariable Long patientId,
            @Parameter(description = "Treatment status") @PathVariable Treatment.TreatmentStatus status) {
        log.info("Received request to get treatments for patient ID: {} with status: {}", patientId, status);
        List<TreatmentDto> treatments = treatmentService.getTreatmentsByPatientIdAndStatus(patientId, status);
        return ResponseEntity.ok(treatments);
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR') or hasRole('NURSE')")
    @Operation(summary = "Get treatments by status", description = "Retrieve treatments filtered by status")
    public ResponseEntity<List<TreatmentDto>> getTreatmentsByStatus(
            @Parameter(description = "Treatment status") @PathVariable Treatment.TreatmentStatus status) {
        log.info("Received request to get treatments by status: {}", status);
        List<TreatmentDto> treatments = treatmentService.getTreatmentsByStatus(status);
        return ResponseEntity.ok(treatments);
    }

    @GetMapping("/doctor/{doctorName}")
    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR') or hasRole('NURSE')")
    @Operation(summary = "Get treatments by doctor", description = "Retrieve treatments filtered by doctor name")
    public ResponseEntity<List<TreatmentDto>> getTreatmentsByDoctor(
            @Parameter(description = "Doctor name") @PathVariable String doctorName) {
        log.info("Received request to get treatments by doctor: {}", doctorName);
        List<TreatmentDto> treatments = treatmentService.getTreatmentsByDoctor(doctorName);
        return ResponseEntity.ok(treatments);
    }

    @GetMapping("/hospital/{hospitalName}")
    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR') or hasRole('NURSE')")
    @Operation(summary = "Get treatments by hospital", description = "Retrieve treatments filtered by hospital name")
    public ResponseEntity<List<TreatmentDto>> getTreatmentsByHospital(
            @Parameter(description = "Hospital name") @PathVariable String hospitalName) {
        log.info("Received request to get treatments by hospital: {}", hospitalName);
        List<TreatmentDto> treatments = treatmentService.getTreatmentsByHospital(hospitalName);
        return ResponseEntity.ok(treatments);
    }

    @GetMapping("/patient/{patientId}/active")
    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR') or hasRole('NURSE') or (hasRole('PATIENT') and @patientService.isPatientOwner(#patientId, authentication.name))")
    @Operation(summary = "Get active treatments for patient", description = "Retrieve active treatments for a specific patient")
    public ResponseEntity<List<TreatmentDto>> getActiveTreatmentsByPatient(
            @Parameter(description = "Patient ID") @PathVariable Long patientId) {
        log.info("Received request to get active treatments for patient ID: {}", patientId);
        List<TreatmentDto> treatments = treatmentService.getActiveTreatmentsByPatient(patientId);
        return ResponseEntity.ok(treatments);
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR') or hasRole('NURSE')")
    @Operation(summary = "Search treatments", description = "Search treatments by keyword")
    public ResponseEntity<List<TreatmentDto>> searchTreatments(
            @Parameter(description = "Search keyword") @RequestParam String keyword) {
        log.info("Received request to search treatments by keyword: {}", keyword);
        List<TreatmentDto> treatments = treatmentService.searchTreatments(keyword);
        return ResponseEntity.ok(treatments);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR')")
    @Operation(summary = "Create a new treatment", description = "Create a new treatment record")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Treatment created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid treatment data"),
        @ApiResponse(responseCode = "403", description = "Access denied"),
        @ApiResponse(responseCode = "404", description = "Patient not found")
    })
    public ResponseEntity<Map<String, Object>> createTreatment(
            @Valid @RequestBody TreatmentDto treatmentDto) {
        log.info("Received request to create treatment for patient ID: {}", treatmentDto.getPatientId());
        
        try {
            TreatmentDto createdTreatment = treatmentService.createTreatment(treatmentDto);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Treatment created successfully");
            response.put("treatment", createdTreatment);
            response.put("status", "SUCCESS");
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (IllegalArgumentException e) {
            log.error("Patient not found for treatment: {}", treatmentDto.getPatientId(), e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(createErrorResponse("Patient not found: " + e.getMessage()));
        } catch (Exception e) {
            log.error("Error creating treatment for patient ID: {}", treatmentDto.getPatientId(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Error creating treatment: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR')")
    @Operation(summary = "Update a treatment", description = "Update an existing treatment record")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Treatment updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid treatment data"),
        @ApiResponse(responseCode = "403", description = "Access denied"),
        @ApiResponse(responseCode = "404", description = "Treatment not found")
    })
    public ResponseEntity<Map<String, Object>> updateTreatment(
            @Parameter(description = "Treatment ID") @PathVariable Long id,
            @Valid @RequestBody TreatmentDto treatmentDto) {
        log.info("Received request to update treatment with ID: {}", id);
        
        try {
            TreatmentDto updatedTreatment = treatmentService.updateTreatment(id, treatmentDto);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Treatment updated successfully");
            response.put("treatment", updatedTreatment);
            response.put("status", "SUCCESS");
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            log.error("Treatment not found: {}", id, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(createErrorResponse("Treatment not found: " + e.getMessage()));
        } catch (Exception e) {
            log.error("Error updating treatment: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Error updating treatment: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR')")
    @Operation(summary = "Delete a treatment", description = "Delete a treatment record")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Treatment deleted successfully"),
        @ApiResponse(responseCode = "403", description = "Access denied"),
        @ApiResponse(responseCode = "404", description = "Treatment not found")
    })
    public ResponseEntity<Map<String, Object>> deleteTreatment(
            @Parameter(description = "Treatment ID") @PathVariable Long id) {
        log.info("Received request to delete treatment with ID: {}", id);
        
        try {
            treatmentService.deleteTreatment(id);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Treatment deleted successfully");
            response.put("treatmentId", id);
            response.put("status", "SUCCESS");
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            log.error("Treatment not found: {}", id, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(createErrorResponse("Treatment not found: " + e.getMessage()));
        } catch (Exception e) {
            log.error("Error deleting treatment: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Error deleting treatment: " + e.getMessage()));
        }
    }

    @GetMapping("/statistics")
    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR')")
    @Operation(summary = "Get treatment statistics", description = "Retrieve various treatment statistics")
    public ResponseEntity<Map<String, Object>> getTreatmentStatistics() {
        log.info("Received request for treatment statistics");
        
        Map<String, Object> statistics = new HashMap<>();
        statistics.put("totalTreatments", treatmentService.getTreatmentCount());
        statistics.put("averageCost", treatmentService.getAverageTreatmentCost());
        statistics.put("treatmentsCreatedToday", treatmentService.getTreatmentsCreatedToday().size());
        
        return ResponseEntity.ok(statistics);
    }

    @GetMapping("/medication/{medication}")
    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR') or hasRole('NURSE')")
    @Operation(summary = "Get treatments by medication", description = "Retrieve treatments containing a specific medication")
    public ResponseEntity<List<TreatmentDto>> getTreatmentsByMedication(
            @Parameter(description = "Medication name") @PathVariable String medication) {
        log.info("Received request to get treatments by medication: {}", medication);
        List<TreatmentDto> treatments = treatmentService.getTreatmentsByMedication(medication);
        return ResponseEntity.ok(treatments);
    }

    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "ERROR");
        response.put("message", message);
        response.put("timestamp", System.currentTimeMillis());
        return response;
    }
}
