package com.healthcare.dashboard.dto;

import com.healthcare.dashboard.model.DashboardData;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardDto {
    private DashboardData.PatientStatistics patientStatistics;
    private DashboardData.TreatmentStatistics treatmentStatistics;
    private List<DashboardData.RecentActivity> recentActivities;
    private Map<String, Object> chartData;
    
    @Builder.Default
    private LocalDateTime lastUpdated = LocalDateTime.now();
}
