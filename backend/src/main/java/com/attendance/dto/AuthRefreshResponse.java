package com.attendance.dto;

/**
 * Response DTO — refresh token success.
 * <p>Returns a new access token. Refresh token rotation happens server-side.
 * Layer: dto.</p>
 */
public record AuthRefreshResponse(String accessToken, long expiresAt) {};
