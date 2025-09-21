package com.healthcare.ingestion.config;


import java.time.LocalDateTime;

public record ApiResponseBody<T>(

        boolean success, String message, T data, LocalDateTime timestamp
) {
    public static <T> ApiResponseBody<T> success(T data, String message) {
        return new ApiResponseBody<>(true, message, data, LocalDateTime.now());
    }

    public static <T> ApiResponseBody<T> failure(String message) {
        return new ApiResponseBody<>(false, message, null, LocalDateTime.now());
    }
}
