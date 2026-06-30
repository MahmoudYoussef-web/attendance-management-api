package com.attendance.dto;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.time.LocalTime;

/**
 * Request DTO — update work schedule fields.
 * <p>All fields are optional; only provided values are updated.
 * Layer: dto.</p>
 */
public record UpdateWorkScheduleRequest(
    @Size(max = 100) String name,
    LocalTime startTime,
    LocalTime endTime,
    @Positive Integer lateAfterMinutes,
    @Positive Integer halfDayAfterMinutes
) {}
