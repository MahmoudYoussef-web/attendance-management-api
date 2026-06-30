package com.attendance.service;

import com.attendance.dto.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
/**
 * Aggregated attendance reports.
 * <p>Uses native SQL via EntityManager (not JPQL) because the queries require
 * LEFT JOINs with conditional aggregation, recursive CTEs (for absence dates),
 * and pagination across grouped results — patterns that are awkward or
 * unsupported in JPQL. Each method returns a Spring Page for consistent API responses.
 * Layer: service.</p>
 */
public class ReportingService {

    private final EntityManager em;

    @Transactional(readOnly = true)
    public Page<DailyAttendanceDto> getDailyReport(LocalDate date, Pageable pageable) {
        String sql = """
            SELECT e.id, e.employee_code, e.first_name, e.last_name,
                   COALESCE(d.name, ''), COALESCE(ar.status, 'ABSENT'), ar.check_in_time
            FROM employees e
            LEFT JOIN departments d ON e.department_id = d.id
            LEFT JOIN attendance_records ar ON e.id = ar.employee_id AND ar.attendance_date = :date
            ORDER BY e.id
            """;
        long total = ((Number) em.createNativeQuery("SELECT COUNT(*) FROM employees")
                .getSingleResult()).longValue();

        Query query = em.createNativeQuery(sql);
        query.setParameter("date", date);
        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());

        List<DailyAttendanceDto> dtos = mapRows(query.getResultList(), row -> {
            Timestamp ts = (Timestamp) row[6];
            return new DailyAttendanceDto(
                    ((Number) row[0]).longValue(),
                    (String) row[1],
                    (String) row[2] + " " + row[3],
                    (String) row[4],
                    (String) row[5],
                    ts != null ? ts.toLocalDateTime() : null
            );
        });
        return new PageImpl<>(dtos, pageable, total);
    }

    @Transactional(readOnly = true)
    public Page<MonthlySummaryDto> getMonthlySummary(Long employeeId, LocalDate start, LocalDate end, Pageable pageable) {
        String sql = """
            SELECT e.id, e.employee_code, e.first_name, e.last_name,
                   COALESCE(d.name, ''), ar.status, COUNT(*)
            FROM attendance_records ar
            JOIN employees e ON ar.employee_id = e.id
            LEFT JOIN departments d ON e.department_id = d.id
            WHERE ar.attendance_date BETWEEN :start AND :end
              AND (:employeeId IS NULL OR e.id = :employeeId)
            GROUP BY e.id, e.employee_code, e.first_name, e.last_name, d.name, ar.status
            ORDER BY e.id
            """;
        String countSql = """
            SELECT COUNT(DISTINCT e.id)
            FROM attendance_records ar
            JOIN employees e ON ar.employee_id = e.id
            WHERE ar.attendance_date BETWEEN :start AND :end
              AND (:employeeId IS NULL OR e.id = :employeeId)
            """;

        Query countQuery = em.createNativeQuery(countSql);
        countQuery.setParameter("start", start);
        countQuery.setParameter("end", end);
        countQuery.setParameter("employeeId", employeeId);
        long total = ((Number) countQuery.getSingleResult()).longValue();

        Query query = em.createNativeQuery(sql);
        query.setParameter("start", start);
        query.setParameter("end", end);
        query.setParameter("employeeId", employeeId);
        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());

        Map<Long, MonthlySummaryDto> map = new LinkedHashMap<>();
        for (Object[] row : (List<Object[]>) query.getResultList()) {
            Long empId = ((Number) row[0]).longValue();
            String status = (String) row[5];
            long count = ((Number) row[6]).longValue();
            MonthlySummaryDto dto = map.computeIfAbsent(empId, k -> new MonthlySummaryDto(
                    empId, (String) row[1], (String) row[2] + " " + row[3], (String) row[4],
                    0, 0, 0, 0, 0));
            switch (status) {
                case "PRESENT" -> dto = new MonthlySummaryDto(empId, dto.employeeCode(), dto.employeeName(),
                        dto.departmentName(), count, dto.late(), dto.halfDay(), dto.absent(), dto.onLeave());
                case "LATE" -> dto = new MonthlySummaryDto(empId, dto.employeeCode(), dto.employeeName(),
                        dto.departmentName(), dto.present(), count, dto.halfDay(), dto.absent(), dto.onLeave());
                case "HALF_DAY" -> dto = new MonthlySummaryDto(empId, dto.employeeCode(), dto.employeeName(),
                        dto.departmentName(), dto.present(), dto.late(), count, dto.absent(), dto.onLeave());
                case "ON_LEAVE" -> dto = new MonthlySummaryDto(empId, dto.employeeCode(), dto.employeeName(),
                        dto.departmentName(), dto.present(), dto.late(), dto.halfDay(), dto.absent(), count);
                default -> dto = new MonthlySummaryDto(empId, dto.employeeCode(), dto.employeeName(),
                        dto.departmentName(), dto.present(), dto.late(), dto.halfDay(), count, dto.onLeave());
            }
            map.put(empId, dto);
        }
        return new PageImpl<>(new ArrayList<>(map.values()), pageable, total);
    }

    @Transactional(readOnly = true)
    public Page<LateArrivalDto> getLateArrivals(LocalDate start, LocalDate end, Pageable pageable) {
        String sql = """
            SELECT ar.attendance_date, ar.check_in_time,
                   e.id, e.employee_code, e.first_name, e.last_name,
                   COALESCE(d.name, ''), COALESCE(ws.start_time, '09:00:00')
            FROM attendance_records ar
            JOIN employees e ON ar.employee_id = e.id
            LEFT JOIN departments d ON e.department_id = d.id
            LEFT JOIN work_schedules ws ON e.schedule_id = ws.id
            WHERE ar.status = 'LATE'
              AND ar.attendance_date BETWEEN :start AND :end
            ORDER BY ar.attendance_date, e.id
            """;
        String countSql = """
            SELECT COUNT(*) FROM attendance_records
            WHERE status = 'LATE' AND attendance_date BETWEEN :start AND :end
            """;

        Query countQuery = em.createNativeQuery(countSql);
        countQuery.setParameter("start", start);
        countQuery.setParameter("end", end);
        long total = ((Number) countQuery.getSingleResult()).longValue();

        Query query = em.createNativeQuery(sql);
        query.setParameter("start", start);
        query.setParameter("end", end);
        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());

        List<LateArrivalDto> dtos = mapRows(query.getResultList(), row -> {
            java.sql.Date sqlDate = (java.sql.Date) row[0];
            Timestamp ts = (Timestamp) row[1];
            LocalDate date = sqlDate.toLocalDate();
            LocalDateTime checkIn = ts.toLocalDateTime();
            LocalTime schedStart = ((java.sql.Time) row[7]).toLocalTime();
            long minsLate = ChronoUnit.MINUTES.between(
                    LocalDateTime.of(date, schedStart), checkIn);
            return new LateArrivalDto(
                    ((Number) row[2]).longValue(),
                    (String) row[3],
                    (String) row[4] + " " + row[5],
                    (String) row[6],
                    date, checkIn, schedStart, minsLate + " min"
            );
        });
        return new PageImpl<>(dtos, pageable, total);
    }

    @Transactional(readOnly = true)
    public Page<AbsenceDto> getAbsences(LocalDate start, LocalDate end, Pageable pageable) {
        String sql = """
            WITH RECURSIVE dates(dt) AS (
                SELECT :start
                UNION ALL
                SELECT dt + INTERVAL 1 DAY FROM dates WHERE dt < :end
            )
            SELECT e.id, e.employee_code, e.first_name, e.last_name,
                   COALESCE(d.name, ''), dates.dt
            FROM employees e
            CROSS JOIN dates
            LEFT JOIN departments d ON e.department_id = d.id
            LEFT JOIN attendance_records ar ON e.id = ar.employee_id AND ar.attendance_date = dates.dt
            WHERE e.status = 'ACTIVE' AND ar.id IS NULL
            ORDER BY dates.dt, e.id
            """;
        String countSql = """
            WITH RECURSIVE dates(dt) AS (
                SELECT :start
                UNION ALL
                SELECT dt + INTERVAL 1 DAY FROM dates WHERE dt < :end
            )
            SELECT COUNT(*)
            FROM employees e
            CROSS JOIN dates
            LEFT JOIN attendance_records ar ON e.id = ar.employee_id AND ar.attendance_date = dates.dt
            WHERE e.status = 'ACTIVE' AND ar.id IS NULL
            """;

        Query countQuery = em.createNativeQuery(countSql);
        countQuery.setParameter("start", start);
        countQuery.setParameter("end", end);
        long total = ((Number) countQuery.getSingleResult()).longValue();

        Query query = em.createNativeQuery(sql);
        query.setParameter("start", start);
        query.setParameter("end", end);
        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());

        List<AbsenceDto> dtos = mapRows(query.getResultList(), row -> {
            java.sql.Date sqlDate = (java.sql.Date) row[5];
            return new AbsenceDto(
                    ((Number) row[0]).longValue(),
                    (String) row[1],
                    (String) row[2] + " " + row[3],
                    (String) row[4],
                    sqlDate.toLocalDate()
            );
        });
        return new PageImpl<>(dtos, pageable, total);
    }

    @Transactional(readOnly = true)
    public Page<DepartmentStatsDto> getDepartmentStats(LocalDate start, LocalDate end, Pageable pageable) {
        String statsSql = """
            SELECT d.id, d.name, ar.status, COUNT(*) AS cnt
            FROM attendance_records ar
            JOIN employees e ON ar.employee_id = e.id
            JOIN departments d ON e.department_id = d.id
            WHERE ar.attendance_date BETWEEN :start AND :end
            GROUP BY d.id, d.name, ar.status
            ORDER BY d.id
            """;
        String countSql = """
            SELECT COUNT(*) FROM (
                SELECT d.id FROM attendance_records ar
                JOIN employees e ON ar.employee_id = e.id
                JOIN departments d ON e.department_id = d.id
                WHERE ar.attendance_date BETWEEN :start AND :end
                GROUP BY d.id
            ) sub
            """;

        Query countQuery = em.createNativeQuery(countSql);
        countQuery.setParameter("start", start);
        countQuery.setParameter("end", end);
        long total = ((Number) countQuery.getSingleResult()).longValue();

        Query query = em.createNativeQuery(statsSql);
        query.setParameter("start", start);
        query.setParameter("end", end);
        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());

        Map<Long, Map<String, Long>> deptStatusCounts = new LinkedHashMap<>();
        Map<Long, String> deptNames = new LinkedHashMap<>();

        for (Object[] row : (List<Object[]>) query.getResultList()) {
            Long deptId = ((Number) row[0]).longValue();
            String deptName = (String) row[1];
            String status = (String) row[2];
            long count = ((Number) row[3]).longValue();
            deptNames.put(deptId, deptName);
            deptStatusCounts.computeIfAbsent(deptId, k -> new HashMap<>()).put(status, count);
        }

        String empCountSql = """
            SELECT department_id, COUNT(*) FROM employees
            WHERE department_id IS NOT NULL
            GROUP BY department_id
            """;
        Map<Long, Long> deptEmpCounts = new HashMap<>();
        for (Object[] row : (List<Object[]>) em.createNativeQuery(empCountSql).getResultList()) {
            deptEmpCounts.put(((Number) row[0]).longValue(), ((Number) row[1]).longValue());
        }

        List<DepartmentStatsDto> dtos = new ArrayList<>();
        for (Map.Entry<Long, Map<String, Long>> entry : deptStatusCounts.entrySet()) {
            Long deptId = entry.getKey();
            Map<String, Long> counts = entry.getValue();
            dtos.add(new DepartmentStatsDto(
                    deptId, deptNames.get(deptId),
                    counts.getOrDefault("PRESENT", 0L),
                    counts.getOrDefault("LATE", 0L),
                    counts.getOrDefault("HALF_DAY", 0L),
                    counts.getOrDefault("ABSENT", 0L),
                    counts.getOrDefault("ON_LEAVE", 0L),
                    deptEmpCounts.getOrDefault(deptId, 0L)
            ));
        }
        return new PageImpl<>(dtos, pageable, total);
    }

    @SuppressWarnings("unchecked")
    private <T> List<T> mapRows(List<?> rows, RowMapper<T> mapper) {
        List<T> result = new ArrayList<>();
        for (Object raw : rows) {
            result.add(mapper.map((Object[]) raw));
        }
        return result;
    }

    @FunctionalInterface
    private interface RowMapper<T> {
        T map(Object[] row);
    }
}
