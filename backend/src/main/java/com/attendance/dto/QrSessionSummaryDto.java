package com.attendance.dto;

import java.time.LocalDateTime;

/**
 * Response DTO — QR session summary (without signing material).
 * <p>Used for listing active/past sessions without exposing the payload signature.
 * Layer: dto.</p>
 */
public record QrSessionSummaryDto(
    Long id, String sessionId, String createdBy,
    LocalDateTime createdAt, LocalDateTime expiresAt, boolean isActive
) {}