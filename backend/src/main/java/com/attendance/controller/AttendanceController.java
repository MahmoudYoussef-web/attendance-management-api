package com.attendance.controller;

import com.attendance.dto.AttendanceDto;
import com.attendance.dto.CheckInRequest;
import com.attendance.dto.QrSessionResponse;
import com.attendance.dto.QrSessionSummaryDto;
import com.attendance.entity.User;
import com.attendance.service.AttendanceService;
import com.attendance.service.QrService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
/**
 * QR session management and employee check-in.
 * <p>QR sessions: SUPER_ADMIN/HR_MANAGER only. Check-in: all authenticated roles.
 * History and today's attendance: admin only.
 * Layer: controller.</p>
 */
public class AttendanceController {

    private final QrService qrService;
    private final AttendanceService attendanceService;

    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'HR_MANAGER')")
    @PostMapping("/qr/sessions")
    public ResponseEntity<QrSessionResponse> createSession(@AuthenticationPrincipal User admin) {
        return ResponseEntity.status(HttpStatus.CREATED).body(qrService.createSession(admin));
    }

    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'HR_MANAGER')")
    @GetMapping("/qr/sessions")
    public ResponseEntity<List<QrSessionSummaryDto>> listSessions() {
        return ResponseEntity.ok(qrService.getSessions());
    }

    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'HR_MANAGER')")
    @DeleteMapping("/qr/sessions/{sessionId}")
    public ResponseEntity<Void> deactivateSession(@PathVariable String sessionId) {
        qrService.deactivateSession(sessionId);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'HR_MANAGER', 'EMPLOYEE')")
    @PostMapping("/check-in")
    public ResponseEntity<AttendanceDto> checkIn(
            @AuthenticationPrincipal User user,
            @RequestBody @Valid CheckInRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(attendanceService.checkIn(user.getId(), req));
    }

    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'HR_MANAGER')")
    @GetMapping
    public ResponseEntity<Page<AttendanceDto>> getHistory(
            @RequestParam(required = false) Long employeeId, Pageable pageable) {
        return ResponseEntity.ok(attendanceService.getHistory(employeeId, pageable));
    }

    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'HR_MANAGER')")
    @GetMapping("/today")
    public ResponseEntity<List<AttendanceDto>> getToday() {
        return ResponseEntity.ok(attendanceService.getTodayAttendance());
    }
}
