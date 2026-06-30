package com.attendance.controller;

import com.attendance.dto.*;
import com.attendance.service.ReportingService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
/**
 * Aggregated attendance reports.
 * <p>All report endpoints require SUPER_ADMIN or HR_MANAGER.
 * Supports daily, monthly, late-arrival, absence, and per-department stats.
 * Layer: controller.</p>
 */
public class ReportingController {

    private final ReportingService reportingService;

    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'HR_MANAGER')")
    @GetMapping("/daily")
    public ResponseEntity<Page<DailyAttendanceDto>> getDailyReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            Pageable pageable) {
        return ResponseEntity.ok(reportingService.getDailyReport(date, pageable));
    }

    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'HR_MANAGER')")
    @GetMapping("/monthly")
    public ResponseEntity<Page<MonthlySummaryDto>> getMonthlySummary(
            @RequestParam(required = false) Long employeeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
            Pageable pageable) {
        return ResponseEntity.ok(reportingService.getMonthlySummary(employeeId, start, end, pageable));
    }

    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'HR_MANAGER')")
    @GetMapping("/late-arrivals")
    public ResponseEntity<Page<LateArrivalDto>> getLateArrivals(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
            Pageable pageable) {
        return ResponseEntity.ok(reportingService.getLateArrivals(start, end, pageable));
    }

    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'HR_MANAGER')")
    @GetMapping("/absences")
    public ResponseEntity<Page<AbsenceDto>> getAbsences(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
            Pageable pageable) {
        return ResponseEntity.ok(reportingService.getAbsences(start, end, pageable));
    }

    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'HR_MANAGER')")
    @GetMapping("/departments")
    public ResponseEntity<Page<DepartmentStatsDto>> getDepartmentStats(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
            Pageable pageable) {
        return ResponseEntity.ok(reportingService.getDepartmentStats(start, end, pageable));
    }
}
