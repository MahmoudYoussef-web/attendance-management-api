package com.attendance.service;

import com.attendance.dto.NotificationDto;
import com.attendance.entity.Employee;
import com.attendance.entity.Notification;
import com.attendance.exception.ResourceNotFoundException;
import com.attendance.mapper.NotificationMapper;
import com.attendance.repository.EmployeeRepository;
import com.attendance.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
/**
 * Employee notification management.
 * <p>Supports create (from leave workflow), list, unread filtering, and mark-as-read.
 * Layer: service.</p>
 */
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final EmployeeRepository employeeRepository;

    @Transactional
    public NotificationDto create(Long employeeId, String message) {
        Employee emp = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", employeeId));
        Notification n = Notification.builder()
                .employee(emp)
                .message(message)
                .build();
        return NotificationMapper.toDto(notificationRepository.save(n));
    }

    @Transactional(readOnly = true)
    public List<NotificationDto> getByEmployee(Long employeeId) {
        return notificationRepository.findByEmployeeIdOrderByCreatedAtDesc(employeeId)
                .stream().map(NotificationMapper::toDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<NotificationDto> getUnread(Long employeeId) {
        return notificationRepository.findByEmployeeIdAndIsReadFalse(employeeId)
                .stream().map(NotificationMapper::toDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(Long employeeId) {
        return notificationRepository.countByEmployeeIdAndIsReadFalse(employeeId);
    }

    @Transactional
    public void markAsRead(Long notificationId) {
        Notification n = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification", notificationId));
        n.setIsRead(true);
        notificationRepository.save(n);
    }

    @Transactional
    public void markAllAsRead(Long employeeId) {
        List<Notification> unread = notificationRepository.findByEmployeeIdAndIsReadFalse(employeeId);
        unread.forEach(n -> n.setIsRead(true));
        notificationRepository.saveAll(unread);
    }
}
