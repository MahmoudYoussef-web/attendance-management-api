package com.attendance.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Response DTO — late arrival detail for reports.
 * <p>Includes scheduled start time and minutes late calculation.
 * Layer: dto.</p>
 */
public record LateArrivalDto(
    Long employeeId, String employeeCode, String employeeName,
    String departmentName, LocalDate date, LocalDateTime checkInTime,
    LocalTime scheduleStart, String minutesLate
) {}
