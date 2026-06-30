package com.attendance.dto;

import jakarta.validation.constraints.Size;
import java.time.LocalDate;

/**
 * Request DTO — update employee fields.
 * <p>All fields are optional; only provided values are updated.
 * Layer: dto.</p>
 */
public record UpdateEmployeeRequest(
    @Size(max = 50) String employeeCode,
    @Size(max = 100) String firstName,
    @Size(max = 100) String lastName,
    @Size(max = 20) String phone,
    Long departmentId,
    @Size(max = 100) String position,
    LocalDate hireDate,
    Long scheduleId
) {}
