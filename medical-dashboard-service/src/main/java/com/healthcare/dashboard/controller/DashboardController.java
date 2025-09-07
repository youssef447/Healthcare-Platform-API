package com.healthcare.dashboard.controller;

import com.healthcare.dashboard.dto.DashboardDto;
import com.healthcare.dashboard.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Controller
@RequestMapping("/")
@Tag(name = "Dashboard", description = "Medical Dashboard Management")
@Slf4j
@RequiredArgsConstructor
public class DashboardController {



    private final DashboardService dashboardService;

    @GetMapping({"/", "/dashboard"})
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'NURSE')")
    public String dashboard(Model model) {
        log.info("Accessing dashboard page");
        try {
            DashboardDto dashboardData = dashboardService.getDashboardData();
            model.addAttribute("dashboardData", dashboardData);
        } catch (Exception e) {
            log.error("Error loading dashboard data", e);
            model.addAttribute("error", "Unable to load dashboard data");
        }
        return "dashboard";
    }

    @GetMapping("/patients")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'NURSE')")
    public String patients(Model model) {
        log.info("Accessing patients page");
        return "patients";
    }

    @GetMapping("/treatments")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'NURSE')")
    public String treatments(Model model) {
        log.info("Accessing treatments page");
        return "treatments";
    }

    @GetMapping("/reports")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    public String reports(Model model) {
        log.info("Accessing reports page");
        return "reports";
    }
}

@RestController
@RequestMapping("/api/dashboard")
@Tag(name = "Dashboard API", description = "Dashboard REST API endpoints")
@SecurityRequirement(name = "bearerAuth")
@Slf4j
@RequiredArgsConstructor
class DashboardRestController {



    private final DashboardService dashboardService;

    @GetMapping("/health")
    @Operation(summary = "Health check endpoint")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "service", "medical-dashboard-service",
            "timestamp", java.time.LocalDateTime.now().toString()
        ));
    }

    @GetMapping("/data")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'NURSE')")
    @Operation(summary = "Get dashboard data", description = "Retrieve comprehensive dashboard data including statistics and charts")
    public ResponseEntity<DashboardDto> getDashboardData() {
        log.info("API request for dashboard data");
        try {
            DashboardDto dashboardData = dashboardService.getDashboardData();
            return ResponseEntity.ok(dashboardData);
        } catch (Exception e) {
            log.error("Error retrieving dashboard data via API", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/analytics/patients")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'NURSE')")
    @Operation(summary = "Get patient analytics", description = "Retrieve detailed patient analytics and statistics")
    public ResponseEntity<Map<String, Object>> getPatientAnalytics() {
        log.info("API request for patient analytics");
        try {
            Map<String, Object> analytics = dashboardService.getPatientAnalytics();
            return ResponseEntity.ok(analytics);
        } catch (Exception e) {
            log.error("Error retrieving patient analytics via API", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/analytics/treatments")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'NURSE')")
    @Operation(summary = "Get treatment analytics", description = "Retrieve detailed treatment analytics and statistics")
    public ResponseEntity<Map<String, Object>> getTreatmentAnalytics() {
        log.info("API request for treatment analytics");
        try {
            Map<String, Object> analytics = dashboardService.getTreatmentAnalytics();
            return ResponseEntity.ok(analytics);
        } catch (Exception e) {
            log.error("Error retrieving treatment analytics via API", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
