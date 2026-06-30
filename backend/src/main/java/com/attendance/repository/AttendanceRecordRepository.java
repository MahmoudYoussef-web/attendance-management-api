package com.attendance.repository;

import com.attendance.entity.AttendanceRecord;
import com.attendance.entity.AttendanceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Data access for {@link AttendanceRecord}.
 * <p>Custom queries for daily attendance lookup, session-based deduplication,
 * and status counting (used for leave backfill validation).
 * Layer: repository.</p>
 */
public interface AttendanceRecordRepository extends JpaRepository<AttendanceRecord, Long> {
    List<AttendanceRecord> findByEmployeeId(Long employeeId);
    Page<AttendanceRecord> findByEmployeeId(Long employeeId, Pageable pageable);
    List<AttendanceRecord> findByAttendanceDate(LocalDate date);
    List<AttendanceRecord> findByEmployeeIdAndAttendanceDate(Long employeeId, LocalDate date);
    Optional<AttendanceRecord> findByEmployeeIdAndSessionId(Long employeeId, String sessionId);
    long countByEmployeeIdAndAttendanceDateAndStatus(Long employeeId, LocalDate date, AttendanceStatus status);
}
