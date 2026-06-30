package com.attendance.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request DTO — create a new user account.
 * <p>rawPassword is hashed before storage. Role is resolved to {@link com.attendance.entity.Role}.
 * Layer: dto.</p>
 */
public record UserCreateRequest(
    @NotBlank @Email String email,
    @NotBlank @Size(min = 8) String rawPassword,
    @NotBlank String role
) {}
