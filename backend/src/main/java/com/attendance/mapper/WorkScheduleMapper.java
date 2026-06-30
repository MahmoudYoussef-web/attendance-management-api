package com.attendance.mapper;

import com.attendance.dto.WorkScheduleDto;
import com.attendance.entity.WorkSchedule;

/**
 * Maps between {@link WorkSchedule} entity and {@link WorkScheduleDto}.
 * <p>Copies threshold values (late/half-day minutes) directly.
 * Layer: mapper.</p>
 */
public class WorkScheduleMapper {

    public static WorkScheduleDto toDto(WorkSchedule ws) {
        return new WorkScheduleDto(
                ws.getId(), ws.getName(), ws.getStartTime(), ws.getEndTime(),
                ws.getLateAfterMinutes(), ws.getHalfDayAfterMinutes(), ws.getCreatedAt()
        );
    }
}
