package com.attendance.dto;

import jakarta.validation.constraints.NotNull;

/**
 * Request DTO — transfer employee to a different department.
 * <p>Reason is optional, used for audit trail.
 * Layer: dto.</p>
 */
public record EmployeeTransferRequest(
    @NotNull Long departmentId,
    String reason
) {}
