package com.attendance.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO — approve or reject a leave request.
 * <p>Action must be "APPROVED" or "REJECTED". Rejection reason required when rejecting.
 * Layer: dto.</p>
 */
public record ReviewLeaveRequest(
    @NotBlank String action,
    String rejectionReason
) {}
