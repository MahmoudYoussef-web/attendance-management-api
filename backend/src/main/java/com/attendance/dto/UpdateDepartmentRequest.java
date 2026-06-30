package com.attendance.dto;

import jakarta.validation.constraints.Size;

/**
 * Request DTO — update department fields.
 * <p>Both fields are optional; only provided values are updated.
 * Layer: dto.</p>
 */
public record UpdateDepartmentRequest(
    @Size(max = 100) String name,
    @Size(max = 255) String description
) {}
