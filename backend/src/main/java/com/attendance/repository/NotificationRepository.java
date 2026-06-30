package com.attendance.repository;

import com.attendance.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Data access for {@link Notification}.
 * <p>Queries for unread counts (used for badge display) and employee
 * notification feed ordered by recency.
 * Layer: repository.</p>
 */
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByEmployeeIdOrderByCreatedAtDesc(Long employeeId);
    List<Notification> findByEmployeeIdAndIsReadFalse(Long employeeId);
    long countByEmployeeIdAndIsReadFalse(Long employeeId);
}
