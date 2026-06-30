package com.attendance.dto;

import java.time.LocalDateTime;

/**
 * Response DTO — QR session with signing material.
 * <p>Includes qrPayload (session UUID) and qrSignature (HMAC-SHA256) for client-side QR generation.
 * Layer: dto.</p>
 */
public record QrSessionResponse(
    Long id, String sessionId, Long createdBy, LocalDateTime expiresAt,
    Boolean isActive, LocalDateTime createdAt, String qrPayload, String qrSignature
) {}
