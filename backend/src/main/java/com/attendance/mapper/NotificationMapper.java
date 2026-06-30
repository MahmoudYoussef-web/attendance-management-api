package com.attendance.mapper;

import com.attendance.dto.NotificationDto;
import com.attendance.entity.Notification;

/**
 * Maps between {@link Notification} entity and {@link NotificationDto}.
 * <p>Extracts employee ID from the notification's employee relationship.
 * Layer: mapper.</p>
 */
public class NotificationMapper {

    public static NotificationDto toDto(Notification n) {
        return new NotificationDto(
                n.getId(),
                n.getEmployee().getId(),
                n.getMessage(),
                n.getIsRead(),
                n.getCreatedAt()
        );
    }
}
