package com.attendance.repository;

import com.attendance.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Data access for {@link Department}.
 * <p>Supports lookup by name for duplicate-check on create/update.
 * Layer: repository.</p>
 */
public interface DepartmentRepository extends JpaRepository<Department, Long> {
    Optional<Department> findByName(String name);
}
