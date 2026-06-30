package com.attendance.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO — user login credentials.
 * <p>Validated: email must be valid format, password must not be blank.
 * Layer: dto.</p>
 */
public record AuthLoginRequest(
    @NotBlank @Email String email,
    @NotBlank String password
) {}
