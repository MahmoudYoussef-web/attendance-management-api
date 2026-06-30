package com.attendance.dto;

import java.time.LocalDateTime;

/**
 * Response DTO — employee notification.
 * <p>Includes read status and creation timestamp for display ordering.
 * Layer: dto.</p>
 */
public record NotificationDto(
    Long id, Long employeeId, String message, Boolean isRead, LocalDateTime createdAt
) {}
