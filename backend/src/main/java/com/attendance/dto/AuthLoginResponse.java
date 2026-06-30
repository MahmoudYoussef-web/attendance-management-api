package com.attendance.dto;

/**
 * Response DTO — login success.
 * <p>Contains access token (JWT), refresh token, and expiry timestamp.
 * Layer: dto.</p>
 */
public record AuthLoginResponse(String accessToken, String refreshToken, long expiresAt) {};
