package com.attendance.dto;

import java.time.LocalDate;

/**
 * Response DTO — employee absence record for reports.
 * <p>Used in the reporting endpoint to list employees with no check-in on a given date.
 * Layer: dto.</p>
 */
public record AbsenceDto(
    Long employeeId, String employeeCode, String employeeName,
    String departmentName, LocalDate date
) {}
