package com.attendance.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request DTO — create a new department.
 * <p>Name is required and max 100 chars; description is optional.
 * Layer: dto.</p>
 */
public record CreateDepartmentRequest(
    @NotBlank @Size(max = 100) String name,
    @Size(max = 255) String description
) {}
