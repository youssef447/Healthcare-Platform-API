package com.healthcare.dashboard.model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public record DashboardData(
        PatientStatistics patientStatistics,
        TreatmentStatistics treatmentStatistics,
        List<RecentActivity> recentActivities,
        Map<String, Object> chartData,
        LocalDateTime lastUpdated
) {
    public DashboardData {
        if (lastUpdated == null) {
            lastUpdated = LocalDateTime.now();
        }
    }

    public record PatientStatistics(
            long totalPatients,
            long activePatients,
            long newPatientsToday,
            long malePatients,
            long femalePatients,
            long patientsWithAllergies,
            long patientsWithoutEmergencyContact
    ) {
    }

    public record TreatmentStatistics(
            long totalTreatments,
            long activeTreatments,
            long completedTreatments,
            long newTreatmentsToday,
            Double averageTreatmentCost
    ) {
    }

    public record RecentActivity(
            String type,
            String description,
            String patientName,
            LocalDateTime timestamp
    ) {
    }
}
