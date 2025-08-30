package com.healthcare.dashboard.dto;

import com.healthcare.dashboard.model.Report;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportDto {
    private String id;

    @NotBlank(message = "Report title is required")
    private String title;

    private String description;

    @NotNull(message = "Report type is required")
    private Report.ReportType type;

    @Builder.Default
    private Report.ReportStatus status = Report.ReportStatus.PENDING;
    
    private String generatedBy;
    
    @Builder.Default
    private LocalDateTime generatedAt = LocalDateTime.now();
    
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Map<String, Object> parameters;
    private Map<String, Object> data;
    private String filePath;
    private String fileFormat;

    public static ReportDto createNew(String title, Report.ReportType type, String generatedBy) {
        return ReportDto.builder()
                .title(title)
                .type(type)
                .generatedBy(generatedBy)
                .build();
    }
}
