package com.attendance.dto;

import java.time.LocalDateTime;

/**
 * Response DTO — daily attendance for reporting.
 * <p>Aggregated per employee for a single date, used in daily report views.
 * Layer: dto.</p>
 */
public record DailyAttendanceDto(
    Long employeeId, String employeeCode, String employeeName,
    String departmentName, String status, LocalDateTime checkInTime
) {}
