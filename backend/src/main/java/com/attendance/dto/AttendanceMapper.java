package com.attendance.dto;

import com.attendance.entity.AttendanceRecord;

/**
 * Maps between {@link AttendanceRecord} entity and {@link AttendanceDto}.
 * <p>Flattens employee fields (code, name) and session reference into the DTO.
 * Layer: mapper.</p>
 */
public class AttendanceMapper {

    public static AttendanceDto toDto(AttendanceRecord rec) {
        return new AttendanceDto(
                rec.getId(),
                rec.getEmployee().getId(),
                rec.getEmployee().getEmployeeCode(),
                rec.getEmployee().getFirstName() + " " + rec.getEmployee().getLastName(),
                rec.getSessionId(),
                rec.getAttendanceDate(),
                rec.getCheckInTime(),
                rec.getStatus().name(),
                rec.getNotes(),
                rec.getCreatedAt()
        );
    }
}
