package com.attendance.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO — QR-based check-in.
 * <p>qrPayload is the session UUID; qrSignature is the HMAC-SHA256 signature for verification.
 * Both are validated server-side to prevent tampered QR codes.
 * Layer: dto.</p>
 */
public record CheckInRequest(
    @NotBlank String qrPayload,
    @NotBlank String qrSignature
) {}
