package com.attendance.dto;

/**
 * Response DTO — per-department attendance statistics.
 * <p>Aggregated counts by status for a given date range.
 * Layer: dto.</p>
 */
public record DepartmentStatsDto(
    Long departmentId, String departmentName, long present, long late,
    long halfDay, long absent, long onLeave, long totalEmployees
) {}
