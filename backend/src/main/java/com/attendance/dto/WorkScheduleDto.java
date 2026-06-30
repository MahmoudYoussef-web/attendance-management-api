package com.attendance.dto;

import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Response DTO — work schedule definition.
 * <p>Includes shift times and attendance threshold configuration.
 * Layer: dto.</p>
 */
public record WorkScheduleDto(
    Long id, String name, LocalTime startTime, LocalTime endTime,
    int lateAfterMinutes, int halfDayAfterMinutes, LocalDateTime createdAt
) {}
