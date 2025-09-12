package com.healthcare.patient.controller;

import com.healthcare.patient.dto.PatientDto;
import com.healthcare.patient.model.Patient;
import com.healthcare.patient.service.PatientService;
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

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/patients")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Patient Management", description = "APIs for managing patient information")
public class PatientController {


    
    private final PatientService patientService;

    @GetMapping("/health")
    @Operation(summary = "Health check endpoint")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "patient-management-service");
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR','NURSE')")
    @Operation(summary = "Get all patients", description = "Retrieve all patients with optional pagination")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Patients retrieved successfully"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<Page<PatientDto>> getAllPatients(Pageable pageable) {
        log.info("Received request to get all patients with pagination");
        Page<PatientDto> patients = patientService.getAllPatients(pageable);
        return ResponseEntity.ok(patients);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR','NURSE') or (hasRole('PATIENT') and @patientService.isPatientOwner(#id, authentication.name))")
    @Operation(summary = "Get patient by ID", description = "Retrieve a specific patient by their ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Patient found"),
        @ApiResponse(responseCode = "404", description = "Patient not found"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<PatientDto> getPatientById(
            @Parameter(description = "Patient ID") @PathVariable Long id) {
        log.info("Received request to get patient by ID: {}", id);
        
        Optional<PatientDto> patient = patientService.getPatientById(id);
        if (patient.isPresent()) {
            return ResponseEntity.ok(patient.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/email/{email}")
    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR','NURSE')")
    @Operation(summary = "Get patient by email", description = "Retrieve a patient by their email address")
    public ResponseEntity<PatientDto> getPatientByEmail(
            @Parameter(description = "Patient email") @PathVariable String email) {
        log.info("Received request to get patient by email: {}", email);
        
        Optional<PatientDto> patient = patientService.getPatientByEmail(email);
        if (patient.isPresent()) {
            return ResponseEntity.ok(patient.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR','NURSE')")
    @Operation(summary = "Search patients by name", description = "Search for patients by first or last name")
    public ResponseEntity<List<PatientDto>> searchPatientsByName(
            @Parameter(description = "Search term") @RequestParam String name) {
        log.info("Received request to search patients by name: {}", name);
        List<PatientDto> patients = patientService.searchPatientsByName(name);
        return ResponseEntity.ok(patients);
    }

    @GetMapping("/gender/{gender}")
    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR','NURSE')")
    @Operation(summary = "Get patients by gender", description = "Retrieve patients filtered by gender")
    public ResponseEntity<List<PatientDto>> getPatientsByGender(
            @Parameter(description = "Patient gender") @PathVariable Patient.Gender gender) {
        log.info("Received request to get patients by gender: {}", gender);
        List<PatientDto> patients = patientService.getPatientsByGender(gender);
        return ResponseEntity.ok(patients);
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR','NURSE')")
    @Operation(summary = "Get patients by status", description = "Retrieve patients filtered by status")
    public ResponseEntity<List<PatientDto>> getPatientsByStatus(
            @Parameter(description = "Patient status") @PathVariable Patient.PatientStatus status) {
        log.info("Received request to get patients by status: {}", status);
        List<PatientDto> patients = patientService.getPatientsByStatus(status);
        return ResponseEntity.ok(patients);
    }

    @GetMapping("/blood-type/{bloodType}")
    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR','NURSE')")
    @Operation(summary = "Get patients by blood type", description = "Retrieve patients filtered by blood type")
    public ResponseEntity<List<PatientDto>> getPatientsByBloodType(
            @Parameter(description = "Blood type") @PathVariable String bloodType) {
        log.info("Received request to get patients by blood type: {}", bloodType);
        List<PatientDto> patients = patientService.getPatientsByBloodType(bloodType);
        return ResponseEntity.ok(patients);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR')")
    @Operation(summary = "Create a new patient", description = "Create a new patient record")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Patient created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid patient data"),
        @ApiResponse(responseCode = "403", description = "Access denied"),
        @ApiResponse(responseCode = "409", description = "Patient already exists")
    })
    public ResponseEntity<Map<String, Object>> createPatient(
            @Valid @RequestBody PatientDto patientDto) {
        log.info("Received request to create patient: {}", patientDto.getFullName());
        
        try {
            PatientDto createdPatient = patientService.createPatient(patientDto);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Patient created successfully");
            response.put("patient", createdPatient);
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

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR') or (hasRole('PATIENT') and @patientService.isPatientOwner(#id, authentication.name))")
    @Operation(summary = "Update a patient", description = "Update an existing patient record")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Patient updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid patient data"),
        @ApiResponse(responseCode = "403", description = "Access denied"),
        @ApiResponse(responseCode = "404", description = "Patient not found")
    })
    public ResponseEntity<Map<String, Object>> updatePatient(
            @Parameter(description = "Patient ID") @PathVariable Long id,
            @Valid @RequestBody PatientDto patientDto) {
        log.info("Received request to update patient with ID: {}", id);
        
        try {
            PatientDto updatedPatient = patientService.updatePatient(id, patientDto);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Patient updated successfully");
            response.put("patient", updatedPatient);
            response.put("status", "SUCCESS");
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            log.error("Patient not found: {}", id, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(createErrorResponse("Patient not found: " + e.getMessage()));
        } catch (Exception e) {
            log.error("Error updating patient: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Error updating patient: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a patient", description = "Delete a patient record")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Patient deleted successfully"),
        @ApiResponse(responseCode = "403", description = "Access denied"),
        @ApiResponse(responseCode = "404", description = "Patient not found")
    })
    public ResponseEntity<Map<String, Object>> deletePatient(
            @Parameter(description = "Patient ID") @PathVariable Long id) {
        log.info("Received request to delete patient with ID: {}", id);
        
        try {
            patientService.deletePatient(id);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Patient deleted successfully");
            response.put("patientId", id);
            response.put("status", "SUCCESS");
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            log.error("Patient not found: {}", id, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(createErrorResponse("Patient not found: " + e.getMessage()));
        } catch (Exception e) {
            log.error("Error deleting patient: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Error deleting patient: " + e.getMessage()));
        }
    }

    @GetMapping("/statistics")
    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR')")
    @Operation(summary = "Get patient statistics", description = "Retrieve various patient statistics")
    public ResponseEntity<Map<String, Object>> getPatientStatistics() {
        log.info("Received request for patient statistics");
        
        Map<String, Object> statistics = new HashMap<>();
        statistics.put("totalPatients", patientService.getPatientCount());
        statistics.put("malePatients", patientService.getPatientCountByGender(Patient.Gender.MALE));
        statistics.put("femalePatients", patientService.getPatientCountByGender(Patient.Gender.FEMALE));
        statistics.put("activePatients", patientService.getPatientCountByStatus(Patient.PatientStatus.ACTIVE));
        statistics.put("inactivePatients", patientService.getPatientCountByStatus(Patient.PatientStatus.INACTIVE));
        statistics.put("patientsCreatedToday", patientService.getPatientsCreatedToday().size());
        
        return ResponseEntity.ok(statistics);
    }

    @GetMapping("/allergies")
    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR','NURSE')")
    @Operation(summary = "Get patients with allergies", description = "Retrieve patients who have recorded allergies")
    public ResponseEntity<List<PatientDto>> getPatientsWithAllergies() {
        log.info("Received request to get patients with allergies");
        List<PatientDto> patients = patientService.getPatientsWithAllergies();
        return ResponseEntity.ok(patients);
    }

    @GetMapping("/missing-emergency-contact")
    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR','NURSE')")
    @Operation(summary = "Get patients without emergency contact", description = "Retrieve patients missing emergency contact information")
    public ResponseEntity<List<PatientDto>> getPatientsWithoutEmergencyContact() {
        log.info("Received request to get patients without emergency contact");
        List<PatientDto> patients = patientService.getPatientsWithoutEmergencyContact();
        return ResponseEntity.ok(patients);
    }

    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "ERROR");
        response.put("message", message);
        response.put("timestamp", System.currentTimeMillis());
        return response;
    }
}
