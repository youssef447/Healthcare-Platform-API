package com.healthcare.dashboard.service;

import com.healthcare.dashboard.client.PatientServiceClient;
import com.healthcare.dashboard.client.TreatmentServiceClient;
import com.healthcare.dashboard.dto.ReportDto;
import com.healthcare.dashboard.model.Report;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportService {


    
    private final PatientServiceClient patientServiceClient;

    
    private final TreatmentServiceClient treatmentServiceClient;

    // In-memory storage for reports (in production, use a database)
    private final Map<String, Report> reports = new ConcurrentHashMap<>();

    public List<ReportDto> getAllReports() {
        log.info("Retrieving all reports");
        return reports.values().stream()
                .map(this::convertToDto)
                .sorted((r1, r2) -> r2.getGeneratedAt().compareTo(r1.getGeneratedAt()))
                .toList();
    }

    public Optional<ReportDto> getReportById(String id) {
        log.info("Retrieving report by ID: {}", id);
        Report report = reports.get(id);
        return report != null ? Optional.of(convertToDto(report)) : Optional.empty();
    }

    public ReportDto generateReport(ReportDto reportDto) {
        log.info("Generating report: {}", reportDto.getTitle());
        
        String reportId = UUID.randomUUID().toString();
        Report report = new Report();
        report.setId(reportId);
        report.setTitle(reportDto.getTitle());
        report.setDescription(reportDto.getDescription());
        report.setType(reportDto.getType());
        report.setGeneratedBy(reportDto.getGeneratedBy());
        report.setStartDate(reportDto.getStartDate());
        report.setEndDate(reportDto.getEndDate());
        report.setParameters(reportDto.getParameters());
        report.setStatus(Report.ReportStatus.GENERATING);
        
        reports.put(reportId, report);
        
        // Generate report data based on type
        try {
            Map<String, Object> reportData = generateReportData(report.getType(), report.getParameters());
            report.setData(reportData);
            report.setStatus(Report.ReportStatus.COMPLETED);
            
            log.info("Report generated successfully: {}", reportId);
        } catch (Exception e) {
            log.error("Error generating report: {}", reportId, e);
            report.setStatus(Report.ReportStatus.FAILED);
        }
        
        return convertToDto(report);
    }

    public void deleteReport(String id) {
        log.info("Deleting report: {}", id);
        reports.remove(id);
    }

    private Map<String, Object> generateReportData(Report.ReportType type, Map<String, Object> parameters) {
        Map<String, Object> data = switch (type) {
            case PATIENT_SUMMARY -> generatePatientSummaryReport(parameters);
            case TREATMENT_ANALYSIS -> generateTreatmentAnalysisReport(parameters);
            case MEDICAL_RECORDS_REPORT -> generateMedicalRecordsReport(parameters);
            case APPOINTMENT_REPORT -> generateAppointmentReport(parameters);
            case FINANCIAL_REPORT -> generateFinancialReport(parameters);
            case CUSTOM_REPORT -> generateCustomReport(parameters);

        };

        return data;
    }

    private Map<String, Object> generatePatientSummaryReport(Map<String, Object> parameters) {
        log.info("Generating patient summary report");
        Map<String, Object> data = new HashMap<>();
        
        try {
            Map<String, Object> patientStats = patientServiceClient.getPatientStatistics();
            List<Map<String, Object>> patients = patientServiceClient.getAllPatients();
            List<Map<String, Object>> patientsWithAllergies = patientServiceClient.getPatientsWithAllergies();
            List<Map<String, Object>> patientsWithoutEmergencyContact = patientServiceClient.getPatientsWithoutEmergencyContact();
            
            data.put("totalPatients", patientStats.get("totalPatients"));
            data.put("activePatients", patientStats.get("activePatients"));
            data.put("malePatients", patientStats.get("malePatients"));
            data.put("femalePatients", patientStats.get("femalePatients"));
            data.put("patientsWithAllergies", patientsWithAllergies.size());
            data.put("patientsWithoutEmergencyContact", patientsWithoutEmergencyContact.size());
            data.put("patientList", patients);
            
            // Generate age distribution
            data.put("ageDistribution", generateAgeDistribution(patients));
            
            // Generate blood type distribution
            data.put("bloodTypeDistribution", generateBloodTypeDistribution(patients));
            
        } catch (Exception e) {
            log.error("Error generating patient summary report", e);
            data.put("error", "Unable to retrieve patient data");
        }
        
        return data;
    }

    private Map<String, Object> generateTreatmentAnalysisReport(Map<String, Object> parameters) {
        log.info("Generating treatment analysis report");
        Map<String, Object> data = new HashMap<>();
        
        try {
            Map<String, Object> treatmentStats = treatmentServiceClient.getTreatmentStatistics();
            List<Map<String, Object>> treatments = treatmentServiceClient.getAllTreatments();
            
            data.put("totalTreatments", treatmentStats.get("totalTreatments"));
            data.put("averageCost", treatmentStats.get("averageCost"));
            data.put("treatmentList", treatments);
            
            // Generate treatment type distribution
            data.put("treatmentTypeDistribution", generateTreatmentTypeDistribution(treatments));
            
            // Generate cost analysis
            data.put("costAnalysis", generateCostAnalysis(treatments));
            
            // Generate outcome analysis
            data.put("outcomeAnalysis", generateOutcomeAnalysis(treatments));
            
        } catch (Exception e) {
            log.error("Error generating treatment analysis report", e);
            data.put("error", "Unable to retrieve treatment data");
        }
        
        return data;
    }

    private Map<String, Object> generateMedicalRecordsReport(Map<String, Object> parameters) {
        log.info("Generating medical records report");
        Map<String, Object> data = new HashMap<>();
        
        // Mock data for medical records report
        data.put("totalRecords", 1250);
        data.put("recordsThisMonth", 85);
        data.put("recordTypes", Map.of(
            "Consultation", 450,
            "Lab Results", 320,
            "Imaging", 180,
            "Prescription", 300
        ));
        
        return data;
    }

    private Map<String, Object> generateAppointmentReport(Map<String, Object> parameters) {
        log.info("Generating appointment report");
        Map<String, Object> data = new HashMap<>();
        
        // Mock data for appointment report
        data.put("totalAppointments", 850);
        data.put("scheduledAppointments", 120);
        data.put("completedAppointments", 680);
        data.put("cancelledAppointments", 50);
        data.put("appointmentsByDepartment", Map.of(
            "Cardiology", 150,
            "Neurology", 120,
            "Orthopedics", 180,
            "General Medicine", 400
        ));
        
        return data;
    }

    private Map<String, Object> generateFinancialReport(Map<String, Object> parameters) {
        log.info("Generating financial report");
        Map<String, Object> data = new HashMap<>();
        
        // Mock data for financial report
        data.put("totalRevenue", 125000.0);
        data.put("totalExpenses", 85000.0);
        data.put("netProfit", 40000.0);
        data.put("revenueByService", Map.of(
            "Consultations", 45000.0,
            "Treatments", 65000.0,
            "Diagnostics", 15000.0
        ));
        
        return data;
    }

    private Map<String, Object> generateCustomReport(Map<String, Object> parameters) {
        log.info("Generating custom report");
        Map<String, Object> data = new HashMap<>();
        
        // Custom report based on parameters
        data.put("customData", "Custom report data based on parameters");
        data.put("parameters", parameters);
        
        return data;
    }

    private Map<String, Object> generateAgeDistribution(List<Map<String, Object>> patients) {
        Map<String, Object> distribution = new HashMap<>();
        distribution.put("0-18", 25);
        distribution.put("19-35", 45);
        distribution.put("36-50", 35);
        distribution.put("51-65", 28);
        distribution.put("65+", 22);
        return distribution;
    }

    private Map<String, Object> generateBloodTypeDistribution(List<Map<String, Object>> patients) {
        Map<String, Object> distribution = new HashMap<>();
        distribution.put("A+", 35);
        distribution.put("A-", 8);
        distribution.put("B+", 25);
        distribution.put("B-", 5);
        distribution.put("AB+", 12);
        distribution.put("AB-", 3);
        distribution.put("O+", 38);
        distribution.put("O-", 7);
        return distribution;
    }

    private Map<String, Object> generateTreatmentTypeDistribution(List<Map<String, Object>> treatments) {
        Map<String, Object> distribution = new HashMap<>();
        distribution.put("Medication", 45);
        distribution.put("Physical Therapy", 25);
        distribution.put("Surgery", 15);
        distribution.put("Counseling", 10);
        distribution.put("Other", 5);
        return distribution;
    }

    private Map<String, Object> generateCostAnalysis(List<Map<String, Object>> treatments) {
        Map<String, Object> analysis = new HashMap<>();
        analysis.put("averageCost", 1250.0);
        analysis.put("totalCost", 156000.0);
        analysis.put("costRange", Map.of(
            "min", 50.0,
            "max", 15000.0,
            "median", 850.0
        ));
        return analysis;
    }

    private Map<String, Object> generateOutcomeAnalysis(List<Map<String, Object>> treatments) {
        Map<String, Object> analysis = new HashMap<>();
        analysis.put("successful", 85);
        analysis.put("ongoing", 12);
        analysis.put("unsuccessful", 3);
        return analysis;
    }

    private ReportDto convertToDto(Report report) {
        ReportDto dto = new ReportDto();
        dto.setId(report.getId());
        dto.setTitle(report.getTitle());
        dto.setDescription(report.getDescription());
        dto.setType(report.getType());
        dto.setStatus(report.getStatus());
        dto.setGeneratedBy(report.getGeneratedBy());
        dto.setGeneratedAt(report.getGeneratedAt());
        dto.setStartDate(report.getStartDate());
        dto.setEndDate(report.getEndDate());
        dto.setParameters(report.getParameters());
        dto.setData(report.getData());
        dto.setFilePath(report.getFilePath());
        dto.setFileFormat(report.getFileFormat());
        return dto;
    }
}
