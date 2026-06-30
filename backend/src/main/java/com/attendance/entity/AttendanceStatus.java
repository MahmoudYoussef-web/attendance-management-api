package com.attendance.entity;

/**
 * Attendance outcome for a daily record.
 * <p>Calculated from check-in time relative to schedule thresholds.
 * ON_LEAVE is set by the leave backfill process, not by check-in.
 * Layer: entity.</p>
 */
public enum AttendanceStatus {
    PRESENT, LATE, HALF_DAY, ABSENT, ON_LEAVE
}
