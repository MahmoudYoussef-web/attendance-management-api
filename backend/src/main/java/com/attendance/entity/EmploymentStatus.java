package com.attendance.entity;

/**
 * Employee lifecycle status.
 * <p>ACTIVE can check in; INACTIVE cannot; TERMINATED is archived.
 * Layer: entity.</p>
 */
public enum EmploymentStatus {
    ACTIVE, INACTIVE, TERMINATED
}
