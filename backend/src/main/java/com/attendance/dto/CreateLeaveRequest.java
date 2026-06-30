package com.attendance.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * Request DTO — submit a leave request.
 * <p>leaveType is resolved to {@link com.attendance.entity.LeaveType}.
 * Date range must not overlap existing approved leave.
 * Layer: dto.</p>
 */
public record CreateLeaveRequest(
    @NotBlank String leaveType,
    @NotNull LocalDate startDate,
    @NotNull LocalDate endDate,
    String reason
) {}
