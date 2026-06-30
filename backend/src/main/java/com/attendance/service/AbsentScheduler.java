package com.attendance.service;

import com.attendance.entity.AttendanceRecord;
import com.attendance.entity.AttendanceStatus;
import com.attendance.entity.Employee;
import com.attendance.entity.EmploymentStatus;
import com.attendance.repository.AttendanceRecordRepository;
import com.attendance.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
/**
 * Scheduled task that auto-marks active employees as ABSENT at end of day (23:59).
 * <p>Runs daily via cron. Employees with no attendance record for today get an ABSENT
 * record created. Uses the employee's schedule start time as a default check-in time.
 * Layer: service.</p>
 */
public class AbsentScheduler {

    private final EmployeeRepository employeeRepository;
    private final AttendanceRecordRepository attendanceRecordRepository;

    @Scheduled(cron = "0 59 23 * * ?")
    @Transactional
    public void markAbsent() {
        LocalDate today = LocalDate.now();
        List<Employee> activeEmployees = employeeRepository.findByStatus(EmploymentStatus.ACTIVE);

        int marked = 0;
        for (Employee emp : activeEmployees) {
            List<AttendanceRecord> existing = attendanceRecordRepository
                    .findByEmployeeIdAndAttendanceDate(emp.getId(), today);
            if (existing.isEmpty()) {
                LocalTime checkIn = emp.getSchedule() != null
                        ? emp.getSchedule().getStartTime()
                        : LocalTime.of(9, 0);
                AttendanceRecord rec = AttendanceRecord.builder()
                        .employee(emp)
                        .sessionId("SCHEDULED-ABSENT")
                        .attendanceDate(today)
                        .checkInTime(LocalDateTime.of(today, checkIn))
                        .status(AttendanceStatus.ABSENT)
                        .notes("Auto-marked absent")
                        .build();
                attendanceRecordRepository.save(rec);
                marked++;
            }
        }
        log.info("Absent scheduler: marked {} employees as ABSENT for {}", marked, today);
    }
}
