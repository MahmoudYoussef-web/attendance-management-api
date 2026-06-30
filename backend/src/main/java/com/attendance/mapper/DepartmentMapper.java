package com.attendance.mapper;

import com.attendance.dto.DepartmentDto;
import com.attendance.entity.Department;

/**
 * Maps between {@link Department} entity and {@link DepartmentDto}.
 * <p>Manual mapper — no MapStruct or reflection. Straight field copy.
 * Layer: mapper.</p>
 */
public class DepartmentMapper {

    public static DepartmentDto toDto(Department dept) {
        return new DepartmentDto(dept.getId(), dept.getName(), dept.getDescription(), dept.getCreatedAt());
    }
}
