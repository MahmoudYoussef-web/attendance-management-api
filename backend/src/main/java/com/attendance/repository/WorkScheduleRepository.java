package com.attendance.repository;

import com.attendance.entity.WorkSchedule;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Data access for {@link WorkSchedule}.
 * <p>Standard CRUD only; schedules are looked up via the employee relation.
 * Layer: repository.</p>
 */
public interface WorkScheduleRepository extends JpaRepository<WorkSchedule, Long> {
}
