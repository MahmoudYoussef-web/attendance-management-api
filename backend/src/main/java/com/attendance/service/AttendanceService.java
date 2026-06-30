package com.attendance.service;

import com.attendance.dto.AttendanceDto;
import com.attendance.dto.AttendanceMapper;
import com.attendance.dto.CheckInRequest;
import com.attendance.entity.*;
import com.attendance.exception.BadRequestException;
import com.attendance.exception.ResourceNotFoundException;
import com.attendance.repository.AttendanceRecordRepository;
import com.attendance.repository.EmployeeRepository;
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
 * Check-in processing and attendance history.
 * <p>Validates QR payload/signature via QrService, prevents duplicate check-in
 * (both in-memory used-set and DB lookup). Status calculation thresholds:
 * — within lateAfterMinutes → PRESENT
 * — lateAfterMinutes..halfDayAfterMinutes → LATE
 * — beyond halfDayAfterMinutes → HALF_DAY
 * (thresholds come from the employee's work schedule).
 * Layer: service.</p>
 */
public class AttendanceService {

    private final AttendanceRecordRepository attendanceRecordRepository;
    private final EmployeeRepository employeeRepository;
    private final QrService qrService;

    @Transactional
    public AttendanceDto checkIn(Long userId, CheckInRequest req) {
        String[] parts = qrService.validateAndParse(req.qrPayload(), req.qrSignature());
        String sessionId = parts[0];
        LocalDate attendanceDate = LocalDate.parse(parts[1]);

        Employee emp = employeeRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", userId));

        if (qrService.hasUsed(sessionId, emp.getId())) {
            throw new BadRequestException("Already checked in with this QR session");
        }

        if (attendanceRecordRepository.findByEmployeeIdAndSessionId(emp.getId(), sessionId).isPresent()) {
            throw new BadRequestException("Already checked in with this QR session");
        }

        AttendanceStatus status = calculateStatus(emp, LocalDateTime.now());
        AttendanceRecord record = AttendanceRecord.builder()
                .employee(emp)
                .sessionId(sessionId)
                .attendanceDate(attendanceDate)
                .checkInTime(LocalDateTime.now())
                .status(status)
                .build();
        attendanceRecordRepository.save(record);

        qrService.markUsed(sessionId, emp.getId());

        return AttendanceMapper.toDto(record);
    }

    @Transactional(readOnly = true)
    public Page<AttendanceDto> getHistory(Long employeeId, Pageable pageable) {
        Page<AttendanceRecord> records;
        if (employeeId != null) {
            records = attendanceRecordRepository.findByEmployeeId(employeeId, pageable);
        } else {
            records = attendanceRecordRepository.findAll(pageable);
        }
        return records.map(AttendanceMapper::toDto);
    }

    @Transactional(readOnly = true)
    public List<AttendanceDto> getTodayAttendance() {
        return attendanceRecordRepository.findByAttendanceDate(LocalDate.now())
                .stream().map(AttendanceMapper::toDto).collect(Collectors.toList());
    }

    private AttendanceStatus calculateStatus(Employee emp, LocalDateTime checkIn) {
        WorkSchedule schedule = emp.getSchedule();
        if (schedule == null) {
            return AttendanceStatus.PRESENT;
        }
        LocalDate date = checkIn.toLocalDate();
        LocalTime start = schedule.getStartTime();
        int lateAfter = schedule.getLateAfterMinutes();
        int halfDayAfter = schedule.getHalfDayAfterMinutes();

        LocalDateTime lateThreshold = LocalDateTime.of(date, start).plusMinutes(lateAfter);
        LocalDateTime halfDayThreshold = LocalDateTime.of(date, start).plusMinutes(halfDayAfter);

        if (!checkIn.isAfter(lateThreshold)) {
            return AttendanceStatus.PRESENT;
        } else if (!checkIn.isAfter(halfDayThreshold)) {
            return AttendanceStatus.LATE;
        } else {
            return AttendanceStatus.HALF_DAY;
        }
    }
}
