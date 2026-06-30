package com.attendance.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Response DTO — employee profile.
 * <p>Includes nested department name and schedule ID for display.
 * Layer: dto.</p>
 */
public record EmployeeDto(
    Long id, Long userId, String employeeCode, String firstName, String lastName,
    String phone, Long departmentId, String departmentName, String position,
    LocalDate hireDate, String status, Long scheduleId, LocalDateTime createdAt
) {}
