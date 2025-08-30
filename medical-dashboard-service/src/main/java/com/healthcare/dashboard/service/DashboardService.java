package com.healthcare.dashboard.service;

import com.healthcare.dashboard.client.PatientServiceClient;
import com.healthcare.dashboard.client.TreatmentServiceClient;
import com.healthcare.dashboard.dto.DashboardDto;
import com.healthcare.dashboard.model.DashboardData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardService {


    private final PatientServiceClient patientServiceClient;


    private final TreatmentServiceClient treatmentServiceClient;

    public DashboardDto getDashboardData() {
        log.info("Retrieving dashboard data");
        
        DashboardDto dashboardDto = new DashboardDto();
        
        try {
            // Get patient statistics
            Map<String, Object> patientStats = patientServiceClient.getPatientStatistics();
            DashboardData.PatientStatistics patientStatistics = new DashboardData.PatientStatistics();
            patientStatistics.setTotalPatients(getLongValue(patientStats, "totalPatients"));
            patientStatistics.setActivePatients(getLongValue(patientStats, "activePatients"));
            patientStatistics.setNewPatientsToday(getLongValue(patientStats, "patientsCreatedToday"));
            patientStatistics.setMalePatients(getLongValue(patientStats, "malePatients"));
            patientStatistics.setFemalePatients(getLongValue(patientStats, "femalePatients"));
            
            dashboardDto.setPatientStatistics(patientStatistics);
            
        } catch (Exception e) {
            log.error("Error retrieving patient statistics", e);
            dashboardDto.setPatientStatistics(new DashboardData.PatientStatistics());
        }

        try {
            // Get treatment statistics
            Map<String, Object> treatmentStats = treatmentServiceClient.getTreatmentStatistics();
            DashboardData.TreatmentStatistics treatmentStatistics = new DashboardData.TreatmentStatistics();
            treatmentStatistics.setTotalTreatments(getLongValue(treatmentStats, "totalTreatments"));
            treatmentStatistics.setNewTreatmentsToday(getLongValue(treatmentStats, "treatmentsCreatedToday"));
            treatmentStatistics.setAverageTreatmentCost(getDoubleValue(treatmentStats, "averageCost"));
            
            dashboardDto.setTreatmentStatistics(treatmentStatistics);
            
        } catch (Exception e) {
            log.error("Error retrieving treatment statistics", e);
            dashboardDto.setTreatmentStatistics(new DashboardData.TreatmentStatistics());
        }

        // Generate chart data
        dashboardDto.setChartData(generateChartData());
        
        // Generate recent activities (mock data for now)
        dashboardDto.setRecentActivities(generateRecentActivities());
        
        dashboardDto.setLastUpdated(LocalDateTime.now());
        
        log.info("Dashboard data retrieved successfully");
        return dashboardDto;
    }

    public Map<String, Object> getPatientAnalytics() {
        log.info("Retrieving patient analytics");
        
        Map<String, Object> analytics = new HashMap<>();
        
        try {
            Map<String, Object> patientStats = patientServiceClient.getPatientStatistics();
            analytics.put("patientStatistics", patientStats);
            
            // Generate additional analytics
            analytics.put("genderDistribution", generateGenderDistribution(patientStats));
            analytics.put("ageDistribution", generateAgeDistribution());
            analytics.put("patientGrowth", generatePatientGrowthData());
            
        } catch (Exception e) {
            log.error("Error retrieving patient analytics", e);
            analytics.put("error", "Unable to retrieve patient analytics");
        }
        
        return analytics;
    }

    public Map<String, Object> getTreatmentAnalytics() {
        log.info("Retrieving treatment analytics");
        
        Map<String, Object> analytics = new HashMap<>();
        
        try {
            Map<String, Object> treatmentStats = treatmentServiceClient.getTreatmentStatistics();
            analytics.put("treatmentStatistics", treatmentStats);
            
            // Generate additional analytics
            analytics.put("treatmentTypes", generateTreatmentTypesData());
            analytics.put("treatmentCosts", generateTreatmentCostData());
            analytics.put("treatmentOutcomes", generateTreatmentOutcomesData());
            
        } catch (Exception e) {
            log.error("Error retrieving treatment analytics", e);
            analytics.put("error", "Unable to retrieve treatment analytics");
        }
        
        return analytics;
    }

    private Map<String, Object> generateChartData() {
        Map<String, Object> chartData = new HashMap<>();
        
        // Patient gender distribution chart
        Map<String, Object> genderChart = new HashMap<>();
        genderChart.put("type", "pie");
        genderChart.put("title", "Patient Gender Distribution");
        genderChart.put("data", List.of(
            Map.of("label", "Male", "value", 45),
            Map.of("label", "Female", "value", 55)
        ));
        chartData.put("genderDistribution", genderChart);
        
        // Monthly patient registrations chart
        Map<String, Object> registrationChart = new HashMap<>();
        registrationChart.put("type", "line");
        registrationChart.put("title", "Monthly Patient Registrations");
        registrationChart.put("data", generateMonthlyRegistrationData());
        chartData.put("monthlyRegistrations", registrationChart);
        
        // Treatment status chart
        Map<String, Object> treatmentChart = new HashMap<>();
        treatmentChart.put("type", "bar");
        treatmentChart.put("title", "Treatment Status Overview");
        treatmentChart.put("data", List.of(
            Map.of("label", "Active", "value", 120),
            Map.of("label", "Completed", "value", 85),
            Map.of("label", "Cancelled", "value", 15),
            Map.of("label", "Paused", "value", 8)
        ));
        chartData.put("treatmentStatus", treatmentChart);
        
        return chartData;
    }

    private List<DashboardData.RecentActivity> generateRecentActivities() {
        List<DashboardData.RecentActivity> activities = new ArrayList<>();
        
        activities.add(new DashboardData.RecentActivity(
            "PATIENT_CREATED", "New patient registered", "John Doe", LocalDateTime.now().minusHours(1)
        ));
        activities.add(new DashboardData.RecentActivity(
            "TREATMENT_STARTED", "Treatment started", "Jane Smith", LocalDateTime.now().minusHours(2)
        ));
        activities.add(new DashboardData.RecentActivity(
            "APPOINTMENT_SCHEDULED", "Appointment scheduled", "Bob Johnson", LocalDateTime.now().minusHours(3)
        ));
        activities.add(new DashboardData.RecentActivity(
            "MEDICAL_RECORD_UPDATED", "Medical record updated", "Alice Brown", LocalDateTime.now().minusHours(4)
        ));
        activities.add(new DashboardData.RecentActivity(
            "TREATMENT_COMPLETED", "Treatment completed", "Charlie Wilson", LocalDateTime.now().minusHours(5)
        ));
        
        return activities;
    }

    private Map<String, Object> generateGenderDistribution(Map<String, Object> patientStats) {
        Map<String, Object> distribution = new HashMap<>();
        distribution.put("male", getLongValue(patientStats, "malePatients"));
        distribution.put("female", getLongValue(patientStats, "femalePatients"));
        distribution.put("total", getLongValue(patientStats, "totalPatients"));
        return distribution;
    }

    private Map<String, Object> generateAgeDistribution() {
        Map<String, Object> distribution = new HashMap<>();
        distribution.put("0-18", 25);
        distribution.put("19-35", 45);
        distribution.put("36-50", 35);
        distribution.put("51-65", 28);
        distribution.put("65+", 22);
        return distribution;
    }

    private List<Map<String, Object>> generatePatientGrowthData() {
        List<Map<String, Object>> growthData = new ArrayList<>();
        String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun"};
        int[] values = {15, 22, 18, 25, 30, 28};
        
        for (int i = 0; i < months.length; i++) {
            growthData.add(Map.of("month", months[i], "patients", values[i]));
        }
        
        return growthData;
    }

    private List<Map<String, Object>> generateMonthlyRegistrationData() {
        List<Map<String, Object>> data = new ArrayList<>();
        String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun"};
        int[] values = {15, 22, 18, 25, 30, 28};
        
        for (int i = 0; i < months.length; i++) {
            data.add(Map.of("month", months[i], "registrations", values[i]));
        }
        
        return data;
    }

    private Map<String, Object> generateTreatmentTypesData() {
        Map<String, Object> types = new HashMap<>();
        types.put("Medication", 45);
        types.put("Physical Therapy", 25);
        types.put("Surgery", 15);
        types.put("Counseling", 10);
        types.put("Other", 5);
        return types;
    }

    private Map<String, Object> generateTreatmentCostData() {
        Map<String, Object> costs = new HashMap<>();
        costs.put("averageCost", 1250.0);
        costs.put("totalCosts", 156000.0);
        costs.put("costByType", Map.of(
            "Medication", 850.0,
            "Physical Therapy", 1200.0,
            "Surgery", 5500.0,
            "Counseling", 180.0
        ));
        return costs;
    }

    private Map<String, Object> generateTreatmentOutcomesData() {
        Map<String, Object> outcomes = new HashMap<>();
        outcomes.put("successful", 85);
        outcomes.put("ongoing", 12);
        outcomes.put("unsuccessful", 3);
        return outcomes;
    }

    private long getLongValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        return 0L;
    }

    private Double getDoubleValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        return null;
    }
}
