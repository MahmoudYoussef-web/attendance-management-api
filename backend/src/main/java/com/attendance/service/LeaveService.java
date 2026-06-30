package com.attendance.service;

import com.attendance.dto.*;
import com.attendance.entity.AttendanceRecord;
import com.attendance.entity.AttendanceStatus;
import com.attendance.entity.Employee;
import com.attendance.entity.LeaveRequest;
import com.attendance.entity.LeaveStatus;
import com.attendance.entity.LeaveType;
import com.attendance.entity.User;
import com.attendance.exception.BadRequestException;
import com.attendance.exception.ResourceNotFoundException;
import com.attendance.mapper.LeaveRequestMapper;
import com.attendance.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
/**
 * Leave request workflow: submit → review (approve/reject).
 * <p>Submission validates date ordering and overlapping leave detection.
 * On APPROVAL, {@link #processLeaveDays} backfills attendance records for the
 * date range as ON_LEAVE (or updates existing records if already checked in).
 * On REJECTION, the rejection reason is stored. Notifications are sent for both outcomes.
 * Layer: service.</p>
 */
public class LeaveService {

    private final LeaveRequestRepository leaveRequestRepository;
    private final AttendanceRecordRepository attendanceRecordRepository;
    private final EmployeeRepository employeeRepository;
    private final NotificationService notificationService;

    @Transactional
    public LeaveRequestDto submit(Long userId, CreateLeaveRequest req) {
        Employee emp = employeeRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", userId));

        if (req.endDate().isBefore(req.startDate())) {
            throw new BadRequestException("End date must be after start date");
        }

        if (leaveRequestRepository.existsByEmployeeIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                emp.getId(), req.endDate(), req.startDate())) {
            throw new BadRequestException("Overlapping leave request exists");
        }

        LeaveType leaveType;
        try {
            leaveType = LeaveType.valueOf(req.leaveType().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid leave type. Must be ANNUAL, SICK, EMERGENCY, or UNPAID");
        }

        LeaveRequest lr = LeaveRequest.builder()
                .employee(emp)
                .leaveType(leaveType)
                .startDate(req.startDate())
                .endDate(req.endDate())
                .reason(req.reason())
                .build();
        return LeaveRequestMapper.toDto(leaveRequestRepository.save(lr));
    }

    @Transactional(readOnly = true)
    public List<LeaveRequestDto> getMyLeaves(Long userId) {
        Employee emp = employeeRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", userId));
        return leaveRequestRepository.findByEmployeeId(emp.getId())
                .stream().map(LeaveRequestMapper::toDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<LeaveRequestDto> getAll(Pageable pageable) {
        return leaveRequestRepository.findAll(pageable).map(LeaveRequestMapper::toDto);
    }

    @Transactional
    public LeaveRequestDto review(Long leaveId, User reviewer, ReviewLeaveRequest req) {
        LeaveRequest lr = leaveRequestRepository.findById(leaveId)
                .orElseThrow(() -> new ResourceNotFoundException("LeaveRequest", leaveId));

        if (lr.getStatus() != LeaveStatus.PENDING) {
            throw new BadRequestException("Leave request is already " + lr.getStatus().name().toLowerCase());
        }

        String action = req.action().toUpperCase();
        if (!action.equals("APPROVE") && !action.equals("REJECT")) {
            throw new BadRequestException("Action must be APPROVE or REJECT");
        }

        if (action.equals("APPROVE")) {
            lr.setStatus(LeaveStatus.APPROVED);
            lr.setApprovedBy(reviewer);
            processLeaveDays(lr);
            notificationService.create(lr.getEmployee().getId(),
                    "Your " + lr.getLeaveType().name() + " leave (" + lr.getStartDate() + " to " + lr.getEndDate() + ") has been approved.");
        } else {
            lr.setStatus(LeaveStatus.REJECTED);
            lr.setRejectionReason(req.rejectionReason());
            notificationService.create(lr.getEmployee().getId(),
                    "Your " + lr.getLeaveType().name() + " leave (" + lr.getStartDate() + " to " + lr.getEndDate() + ") has been rejected."
                    + (req.rejectionReason() != null ? " Reason: " + req.rejectionReason() : ""));
        }

        return LeaveRequestMapper.toDto(leaveRequestRepository.save(lr));
    }

    private void processLeaveDays(LeaveRequest lr) {
        LocalDate start = lr.getStartDate();
        LocalDate end = lr.getEndDate();
        LocalTime defaultCheckIn = lr.getEmployee().getSchedule() != null
                ? lr.getEmployee().getSchedule().getStartTime()
                : LocalTime.of(9, 0);
        for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
            List<AttendanceRecord> existing = attendanceRecordRepository
                    .findByEmployeeIdAndAttendanceDate(lr.getEmployee().getId(), date);
            if (!existing.isEmpty()) {
                existing.forEach(rec -> rec.setStatus(AttendanceStatus.ON_LEAVE));
                attendanceRecordRepository.saveAll(existing);
            } else {
                AttendanceRecord rec = AttendanceRecord.builder()
                        .employee(lr.getEmployee())
                        .sessionId("LEAVE-" + lr.getId())
                        .attendanceDate(date)
                        .checkInTime(LocalDateTime.of(date, defaultCheckIn))
                        .status(AttendanceStatus.ON_LEAVE)
                        .build();
                attendanceRecordRepository.save(rec);
            }
        }
    }
}
