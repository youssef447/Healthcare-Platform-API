package com.healthcare.ingestion.dto;


import lombok.Builder;

import java.time.Instant;

@Builder
public record JobResponseDTO(String message, String fileName, String status, Long jobExecutionId, Instant timestamp) {
}
