package com.healthcare.dashboard.controller;

import com.healthcare.dashboard.dto.ReportDto;
import com.healthcare.dashboard.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/reports")
@Tag(name = "Reports", description = "Medical Reports Management")
@SecurityRequirement(name = "bearerAuth")
public class ReportController {

    private static final Logger logger = LoggerFactory.getLogger(ReportController.class);

    @Autowired
    private ReportService reportService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    @Operation(summary = "Get all reports", description = "Retrieve all generated reports")
    public ResponseEntity<List<ReportDto>> getAllReports() {
        logger.info("API request to get all reports");
        try {
            List<ReportDto> reports = reportService.getAllReports();
            return ResponseEntity.ok(reports);
        } catch (Exception e) {
            logger.error("Error retrieving reports", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    @Operation(summary = "Get report by ID", description = "Retrieve a specific report by its ID")
    public ResponseEntity<ReportDto> getReportById(@PathVariable String id) {
        logger.info("API request to get report by ID: {}", id);
        try {
            Optional<ReportDto> report = reportService.getReportById(id);
            return report.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            logger.error("Error retrieving report by ID: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    @Operation(summary = "Generate new report", description = "Generate a new medical report")
    public ResponseEntity<ReportDto> generateReport(@Valid @RequestBody ReportDto reportDto) {
        logger.info("API request to generate new report: {}", reportDto.getTitle());
        try {
            ReportDto generatedReport = reportService.generateReport(reportDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(generatedReport);
        } catch (Exception e) {
            logger.error("Error generating report", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete report", description = "Delete a report by its ID")
    public ResponseEntity<Void> deleteReport(@PathVariable String id) {
        logger.info("API request to delete report: {}", id);
        try {
            reportService.deleteReport(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            logger.error("Error deleting report: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/health")
    @Operation(summary = "Health check endpoint")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Reports service is healthy");
    }
}
