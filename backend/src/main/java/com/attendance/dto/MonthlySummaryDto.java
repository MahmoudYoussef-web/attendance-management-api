package com.attendance.dto;

/**
 * Response DTO — monthly attendance summary per employee.
 * <p>Aggregated counts by attendance status for a given month.
 * Layer: dto.</p>
 */
public record MonthlySummaryDto(
    Long employeeId, String employeeCode, String employeeName,
    String departmentName, long present, long late, long halfDay,
    long absent, long onLeave
) {}
