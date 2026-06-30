package com.attendance.mapper;

import com.attendance.dto.EmployeeDto;
import com.attendance.entity.Employee;

/**
 * Maps between {@link Employee} entity and {@link EmployeeDto}.
 * <p>Flattens nested entities (department, schedule, user) into the DTO.
 * Layer: mapper.</p>
 */
public class EmployeeMapper {

    public static EmployeeDto toDto(Employee emp) {
        return new EmployeeDto(
                emp.getId(),
                emp.getUser().getId(),
                emp.getEmployeeCode(),
                emp.getFirstName(),
                emp.getLastName(),
                emp.getPhone(),
                emp.getDepartment() != null ? emp.getDepartment().getId() : null,
                emp.getDepartment() != null ? emp.getDepartment().getName() : null,
                emp.getPosition(),
                emp.getHireDate(),
                emp.getStatus().name(),
                emp.getSchedule() != null ? emp.getSchedule().getId() : null,
                emp.getCreatedAt()
        );
    }
}
