package com.attendance.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "work_schedules")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
/**
 * Work shift definition — maps to {@code work_schedules} table.
 * <p>Defines start/end times and thresholds for attendance status calculation:
 * late_after_minutes and half_day_after_minutes control when a check-in is
 * considered LATE vs HALF_DAY vs ABSENT.
 * Layer: entity.</p>
 */
public class WorkSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(name = "late_after_minutes", nullable = false)
    private int lateAfterMinutes = 15;

    @Column(name = "half_day_after_minutes", nullable = false)
    private int halfDayAfterMinutes = 180;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}
