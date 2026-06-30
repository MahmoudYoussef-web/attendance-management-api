package com.attendance.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO — standard API error body.
 * <p>Contains HTTP status, error code, human-readable message, timestamp, and optional field-level validation errors.
 * Layer: dto.</p>
 */
public record ErrorResponse(
    int status, String error, String message, LocalDateTime timestamp, List<FieldError> fieldErrors
) {
    public ErrorResponse(int status, String error, String message) {
        this(status, error, message, LocalDateTime.now(), null);
    }

    public record FieldError(String field, String message) {}
}
