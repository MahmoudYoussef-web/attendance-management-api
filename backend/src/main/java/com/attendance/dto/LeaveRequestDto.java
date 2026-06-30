package com.attendance.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Response DTO — leave request details.
 * <p>Includes status, approval info, and timestamps for the full leave lifecycle.
 * Layer: dto.</p>
 */
public record LeaveRequestDto(
    Long id, Long employeeId, String employeeName, String leaveType,
    LocalDate startDate, LocalDate endDate, String reason, String status,
    String rejectionReason, Long approvedBy, LocalDateTime createdAt, LocalDateTime updatedAt
) {}
