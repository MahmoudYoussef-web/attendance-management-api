package com.attendance.repository;

import com.attendance.entity.Employee;
import com.attendance.entity.EmploymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Data access for {@link Employee}.
 * <p>Custom queries for user/employee-code lookup, department listing,
 * status filtering, and existence checks (used before department deletion).
 * Layer: repository.</p>
 */
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByUserId(Long userId);
    Optional<Employee> findByEmployeeCode(String employeeCode);
    List<Employee> findByDepartmentId(Long departmentId);
    List<Employee> findByStatus(EmploymentStatus status);
    boolean existsByDepartmentId(Long departmentId);
}
