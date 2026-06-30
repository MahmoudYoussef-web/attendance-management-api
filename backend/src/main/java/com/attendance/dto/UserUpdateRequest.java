package com.attendance.dto;

import jakarta.validation.constraints.Email;

/**
 * Request DTO — update user account fields.
 * <p>All fields optional. Changes to role or lock state are admin-only operations.
 * Layer: dto.</p>
 */
public record UserUpdateRequest(
    @Email String email,
    String role,
    Boolean enabled,
    Boolean locked
) {}
