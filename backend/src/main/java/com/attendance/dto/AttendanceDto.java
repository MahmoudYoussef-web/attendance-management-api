package com.attendance.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Response DTO — single attendance record.
 * <p>Returned from check-in and attendance listing endpoints.
 * Layer: dto.</p>
 */
public record AttendanceDto(
    Long id, Long employeeId, String employeeCode, String employeeName,
    String sessionId, LocalDate attendanceDate, LocalDateTime checkInTime,
    String status, String notes, LocalDateTime createdAt
) {}
