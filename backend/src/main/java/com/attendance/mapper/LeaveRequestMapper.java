package com.attendance.mapper;

import com.attendance.dto.LeaveRequestDto;
import com.attendance.entity.LeaveRequest;

/**
 * Maps between {@link LeaveRequest} entity and {@link LeaveRequestDto}.
 * <p>Flattens employee name and approved-by user ID. Null-safe for unapproved requests.
 * Layer: mapper.</p>
 */
public class LeaveRequestMapper {

    public static LeaveRequestDto toDto(LeaveRequest lr) {
        return new LeaveRequestDto(
                lr.getId(),
                lr.getEmployee().getId(),
                lr.getEmployee().getFirstName() + " " + lr.getEmployee().getLastName(),
                lr.getLeaveType().name(),
                lr.getStartDate(),
                lr.getEndDate(),
                lr.getReason(),
                lr.getStatus().name(),
                lr.getRejectionReason(),
                lr.getApprovedBy() != null ? lr.getApprovedBy().getId() : null,
                lr.getCreatedAt(),
                lr.getUpdatedAt()
        );
    }
}
