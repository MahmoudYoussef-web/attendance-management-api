package com.attendance.entity;

/**
 * Leave request lifecycle state.
 * <p>PENDING = awaiting HR approval; APPROVED = leave confirmed;
 * REJECTED = denied with optional rejection reason.
 * Layer: entity.</p>
 */
public enum LeaveStatus {
    PENDING, APPROVED, REJECTED
}
