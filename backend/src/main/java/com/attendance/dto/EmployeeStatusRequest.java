package com.attendance.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO — update employee employment status.
 * <p>Status is resolved to {@link com.attendance.entity.EmploymentStatus}.
 * Layer: dto.</p>
 */
public record EmployeeStatusRequest(
    @NotBlank String status
) {
    public String getStatus() { return status; }
}
