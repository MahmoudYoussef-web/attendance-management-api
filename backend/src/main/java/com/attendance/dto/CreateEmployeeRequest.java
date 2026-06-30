package com.attendance.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

/**
 * Request DTO — create a new employee profile.
 * <p>Links an existing user (by userId) with work details: code, name, department, schedule.
 * Layer: dto.</p>
 */
public record CreateEmployeeRequest(
    @NotNull Long userId,
    @NotBlank @Size(max = 50) String employeeCode,
    @NotBlank @Size(max = 100) String firstName,
    @NotBlank @Size(max = 100) String lastName,
    @Size(max = 20) String phone,
    Long departmentId,
    @Size(max = 100) String position,
    @NotNull LocalDate hireDate,
    Long scheduleId
) {}
