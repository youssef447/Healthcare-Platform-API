package com.healthcare.dashboard.model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class DashboardData {

    private PatientStatistics patientStatistics;
    private TreatmentStatistics treatmentStatistics;
    private List<RecentActivity> recentActivities;
    private Map<String, Object> chartData;
    private LocalDateTime lastUpdated;

    public static class PatientStatistics {
        private long totalPatients;
        private long activePatients;
        private long newPatientsToday;
        private long malePatients;
        private long femalePatients;
        private long patientsWithAllergies;
        private long patientsWithoutEmergencyContact;

        // Constructors
        public PatientStatistics() {}

        public PatientStatistics(long totalPatients, long activePatients, long newPatientsToday) {
            this.totalPatients = totalPatients;
            this.activePatients = activePatients;
            this.newPatientsToday = newPatientsToday;
        }

        // Getters and Setters
        public long getTotalPatients() { return totalPatients; }
        public void setTotalPatients(long totalPatients) { this.totalPatients = totalPatients; }

        public long getActivePatients() { return activePatients; }
        public void setActivePatients(long activePatients) { this.activePatients = activePatients; }

        public long getNewPatientsToday() { return newPatientsToday; }
        public void setNewPatientsToday(long newPatientsToday) { this.newPatientsToday = newPatientsToday; }

        public long getMalePatients() { return malePatients; }
        public void setMalePatients(long malePatients) { this.malePatients = malePatients; }

        public long getFemalePatients() { return femalePatients; }
        public void setFemalePatients(long femalePatients) { this.femalePatients = femalePatients; }

        public long getPatientsWithAllergies() { return patientsWithAllergies; }
        public void setPatientsWithAllergies(long patientsWithAllergies) { this.patientsWithAllergies = patientsWithAllergies; }

        public long getPatientsWithoutEmergencyContact() { return patientsWithoutEmergencyContact; }
        public void setPatientsWithoutEmergencyContact(long patientsWithoutEmergencyContact) { this.patientsWithoutEmergencyContact = patientsWithoutEmergencyContact; }
    }

    public static class TreatmentStatistics {
        private long totalTreatments;
        private long activeTreatments;
        private long completedTreatments;
        private long newTreatmentsToday;
        private Double averageTreatmentCost;

        // Constructors
        public TreatmentStatistics() {}

        public TreatmentStatistics(long totalTreatments, long activeTreatments, long completedTreatments) {
            this.totalTreatments = totalTreatments;
            this.activeTreatments = activeTreatments;
            this.completedTreatments = completedTreatments;
        }

        // Getters and Setters
        public long getTotalTreatments() { return totalTreatments; }
        public void setTotalTreatments(long totalTreatments) { this.totalTreatments = totalTreatments; }

        public long getActiveTreatments() { return activeTreatments; }
        public void setActiveTreatments(long activeTreatments) { this.activeTreatments = activeTreatments; }

        public long getCompletedTreatments() { return completedTreatments; }
        public void setCompletedTreatments(long completedTreatments) { this.completedTreatments = completedTreatments; }

        public long getNewTreatmentsToday() { return newTreatmentsToday; }
        public void setNewTreatmentsToday(long newTreatmentsToday) { this.newTreatmentsToday = newTreatmentsToday; }

        public Double getAverageTreatmentCost() { return averageTreatmentCost; }
        public void setAverageTreatmentCost(Double averageTreatmentCost) { this.averageTreatmentCost = averageTreatmentCost; }
    }

    public static class RecentActivity {
        private String type;
        private String description;
        private String patientName;
        private LocalDateTime timestamp;

        // Constructors
        public RecentActivity() {}

        public RecentActivity(String type, String description, String patientName, LocalDateTime timestamp) {
            this.type = type;
            this.description = description;
            this.patientName = patientName;
            this.timestamp = timestamp;
        }

        // Getters and Setters
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public String getPatientName() { return patientName; }
        public void setPatientName(String patientName) { this.patientName = patientName; }

        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    }

    // Constructors
    public DashboardData() {
        this.lastUpdated = LocalDateTime.now();
    }

    // Getters and Setters
    public PatientStatistics getPatientStatistics() { return patientStatistics; }
    public void setPatientStatistics(PatientStatistics patientStatistics) { this.patientStatistics = patientStatistics; }

    public TreatmentStatistics getTreatmentStatistics() { return treatmentStatistics; }
    public void setTreatmentStatistics(TreatmentStatistics treatmentStatistics) { this.treatmentStatistics = treatmentStatistics; }

    public List<RecentActivity> getRecentActivities() { return recentActivities; }
    public void setRecentActivities(List<RecentActivity> recentActivities) { this.recentActivities = recentActivities; }

    public Map<String, Object> getChartData() { return chartData; }
    public void setChartData(Map<String, Object> chartData) { this.chartData = chartData; }

    public LocalDateTime getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(LocalDateTime lastUpdated) { this.lastUpdated = lastUpdated; }
}
