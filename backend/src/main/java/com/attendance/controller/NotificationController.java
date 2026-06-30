package com.attendance.controller;

import com.attendance.dto.NotificationDto;
import com.attendance.entity.Employee;
import com.attendance.entity.User;
import com.attendance.exception.ResourceNotFoundException;
import com.attendance.repository.EmployeeRepository;
import com.attendance.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
/**
 * Employee notification feed.
 * <p>All authenticated roles can view own notifications and mark them as read.
 * Employee ID is resolved from the authenticated user.
 * Layer: controller.</p>
 */
public class NotificationController {

    private final NotificationService notificationService;
    private final EmployeeRepository employeeRepository;

    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'HR_MANAGER', 'EMPLOYEE')")
    @GetMapping
    public ResponseEntity<List<NotificationDto>> getMy(@AuthenticationPrincipal User user) {
        Employee emp = employeeRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee", user.getId()));
        return ResponseEntity.ok(notificationService.getByEmployee(emp.getId()));
    }

    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'HR_MANAGER', 'EMPLOYEE')")
    @GetMapping("/unread")
    public ResponseEntity<List<NotificationDto>> getUnread(@AuthenticationPrincipal User user) {
        Employee emp = employeeRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee", user.getId()));
        return ResponseEntity.ok(notificationService.getUnread(emp.getId()));
    }

    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'HR_MANAGER', 'EMPLOYEE')")
    @GetMapping("/unread-count")
    public ResponseEntity<Long> getUnreadCount(@AuthenticationPrincipal User user) {
        Employee emp = employeeRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee", user.getId()));
        return ResponseEntity.ok(notificationService.getUnreadCount(emp.getId()));
    }

    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'HR_MANAGER', 'EMPLOYEE')")
    @PutMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'HR_MANAGER', 'EMPLOYEE')")
    @PutMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead(@AuthenticationPrincipal User user) {
        Employee emp = employeeRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee", user.getId()));
        notificationService.markAllAsRead(emp.getId());
        return ResponseEntity.noContent().build();
    }
}
