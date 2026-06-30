package com.attendance.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.time.LocalTime;

/**
 * Request DTO — create a work schedule.
 * <p>Defines shift start/end times and the late/half-day thresholds in minutes.
 * Layer: dto.</p>
 */
public record CreateWorkScheduleRequest(
    @NotBlank @Size(max = 100) String name,
    @NotNull LocalTime startTime,
    @NotNull LocalTime endTime,
    @Positive int lateAfterMinutes,
    @Positive int halfDayAfterMinutes
) {}
