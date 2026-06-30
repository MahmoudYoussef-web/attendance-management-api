package com.attendance.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request DTO — password change.
 * <p>New password must be at least 8 characters. Old password validated against current hash.
 * Layer: dto.</p>
 */
public record ChangePasswordRequest(
    @NotBlank String oldPassword,
    @NotBlank @Size(min = 8) String newPassword
) {}
