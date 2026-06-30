package com.attendance.entity;

/**
 * System access roles, ordered by privilege.
 * <p>SUPER_ADMIN: full access; HR_MANAGER: can manage employees, leave,
 * and view reports; EMPLOYEE: self-service (check-in, own leave requests).
 * Layer: entity.</p>
 */
public enum Role {
    SUPER_ADMIN,
    HR_MANAGER,
    EMPLOYEE
}
