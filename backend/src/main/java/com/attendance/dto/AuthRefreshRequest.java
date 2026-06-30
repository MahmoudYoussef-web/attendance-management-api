package com.attendance.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO — refresh token exchange.
 * <p>Validated: refresh token must not be blank.
 * Layer: dto.</p>
 */
public record AuthRefreshRequest(
    @NotBlank String refreshToken
) {}
