package com.attendance.repository;

import com.attendance.entity.LeaveRequest;
import com.attendance.entity.LeaveStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

/**
 * Data access for {@link LeaveRequest}.
 * <p>Custom queries for leave listing, status filtering, and date-overlap
 * detection (prevents booking leave over existing approved leave).
 * Layer: repository.</p>
 */
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {
    Page<LeaveRequest> findByEmployeeId(Long employeeId, Pageable pageable);
    List<LeaveRequest> findByEmployeeId(Long employeeId);
    List<LeaveRequest> findByStatus(LeaveStatus status);
    List<LeaveRequest> findByEmployeeIdAndStatus(Long employeeId, LeaveStatus status);
    boolean existsByEmployeeIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
            Long employeeId, LocalDate endDate, LocalDate startDate);
}
