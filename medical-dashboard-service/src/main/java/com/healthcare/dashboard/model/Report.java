package com.healthcare.dashboard.model;

import java.time.LocalDateTime;
import java.util.Map;

public class Report {

    private String id;
    private String title;
    private String description;
    private ReportType type;
    private ReportStatus status;
    private String generatedBy;
    private LocalDateTime generatedAt;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Map<String, Object> parameters;
    private Map<String, Object> data;
    private String filePath;
    private String fileFormat;

    public enum ReportType {
        PATIENT_SUMMARY,
        TREATMENT_ANALYSIS,
        MEDICAL_RECORDS_REPORT,
        APPOINTMENT_REPORT,
        FINANCIAL_REPORT,
        CUSTOM_REPORT
    }

    public enum ReportStatus {
        PENDING,
        GENERATING,
        COMPLETED,
        FAILED
    }

    // Constructors
    public Report() {
        this.generatedAt = LocalDateTime.now();
        this.status = ReportStatus.PENDING;
    }

    public Report(String title, ReportType type, String generatedBy) {
        this();
        this.title = title;
        this.type = type;
        this.generatedBy = generatedBy;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public ReportType getType() { return type; }
    public void setType(ReportType type) { this.type = type; }

    public ReportStatus getStatus() { return status; }
    public void setStatus(ReportStatus status) { this.status = status; }

    public String getGeneratedBy() { return generatedBy; }
    public void setGeneratedBy(String generatedBy) { this.generatedBy = generatedBy; }

    public LocalDateTime getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(LocalDateTime generatedAt) { this.generatedAt = generatedAt; }

    public LocalDateTime getStartDate() { return startDate; }
    public void setStartDate(LocalDateTime startDate) { this.startDate = startDate; }

    public LocalDateTime getEndDate() { return endDate; }
    public void setEndDate(LocalDateTime endDate) { this.endDate = endDate; }

    public Map<String, Object> getParameters() { return parameters; }
    public void setParameters(Map<String, Object> parameters) { this.parameters = parameters; }

    public Map<String, Object> getData() { return data; }
    public void setData(Map<String, Object> data) { this.data = data; }

    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }

    public String getFileFormat() { return fileFormat; }
    public void setFileFormat(String fileFormat) { this.fileFormat = fileFormat; }
}
