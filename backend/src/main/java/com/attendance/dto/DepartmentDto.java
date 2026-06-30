package com.attendance.dto;

import java.time.LocalDateTime;

/**
 * Response DTO — department summary.
 * <p>Returned from department CRUD endpoints.
 * Layer: dto.</p>
 */
public record DepartmentDto(Long id, String name, String description, LocalDateTime createdAt) {}
