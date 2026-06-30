package com.attendance.service;

import com.attendance.dto.*;
import com.attendance.entity.Department;
import com.attendance.entity.Employee;
import com.attendance.entity.EmploymentStatus;
import com.attendance.entity.User;
import com.attendance.entity.WorkSchedule;
import com.attendance.exception.BadRequestException;
import com.attendance.exception.ResourceNotFoundException;
import com.attendance.mapper.EmployeeMapper;
import com.attendance.repository.DepartmentRepository;
import com.attendance.repository.EmployeeRepository;
import com.attendance.repository.UserRepository;
import com.attendance.repository.WorkScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
/**
 * Employee profile management with linked entity validation.
 * <p>Prevents duplicate employee codes and double-profile per user on create.
 * Supports transfer between departments and employment status changes.
 * Layer: service.</p>
 */
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final WorkScheduleRepository workScheduleRepository;

    @Transactional
    public EmployeeDto create(CreateEmployeeRequest req) {
        if (employeeRepository.findByEmployeeCode(req.employeeCode()).isPresent()) {
            throw new BadRequestException("Employee code already exists");
        }
        if (employeeRepository.findByUserId(req.userId()).isPresent()) {
            throw new BadRequestException("User already has an employee profile");
        }
        User user = userRepository.findById(req.userId())
                .orElseThrow(() -> new ResourceNotFoundException("User", req.userId()));

        Department dept = null;
        if (req.departmentId() != null) {
            dept = departmentRepository.findById(req.departmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department", req.departmentId()));
        }

        WorkSchedule schedule = null;
        if (req.scheduleId() != null) {
            schedule = workScheduleRepository.findById(req.scheduleId())
                    .orElseThrow(() -> new ResourceNotFoundException("WorkSchedule", req.scheduleId()));
        }

        Employee emp = Employee.builder()
                .user(user)
                .employeeCode(req.employeeCode())
                .firstName(req.firstName())
                .lastName(req.lastName())
                .phone(req.phone())
                .department(dept)
                .position(req.position())
                .hireDate(req.hireDate())
                .schedule(schedule)
                .build();
        return EmployeeMapper.toDto(employeeRepository.save(emp));
    }

    @Transactional(readOnly = true)
    public Page<EmployeeDto> getAll(Pageable pageable) {
        return employeeRepository.findAll(pageable).map(EmployeeMapper::toDto);
    }

    @Transactional(readOnly = true)
    public EmployeeDto getById(Long id) {
        Employee emp = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", id));
        return EmployeeMapper.toDto(emp);
    }

    @Transactional
    public EmployeeDto update(Long id, UpdateEmployeeRequest req) {
        Employee emp = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", id));
        if (req.employeeCode() != null && !req.employeeCode().equals(emp.getEmployeeCode())) {
            if (employeeRepository.findByEmployeeCode(req.employeeCode()).isPresent()) {
                throw new BadRequestException("Employee code already exists");
            }
            emp.setEmployeeCode(req.employeeCode());
        }
        if (req.firstName() != null) emp.setFirstName(req.firstName());
        if (req.lastName() != null) emp.setLastName(req.lastName());
        if (req.phone() != null) emp.setPhone(req.phone());
        if (req.departmentId() != null) {
            Department dept = departmentRepository.findById(req.departmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department", req.departmentId()));
            emp.setDepartment(dept);
        }
        if (req.position() != null) emp.setPosition(req.position());
        if (req.hireDate() != null) emp.setHireDate(req.hireDate());
        if (req.scheduleId() != null) {
            WorkSchedule schedule = workScheduleRepository.findById(req.scheduleId())
                    .orElseThrow(() -> new ResourceNotFoundException("WorkSchedule", req.scheduleId()));
            emp.setSchedule(schedule);
        }
        return EmployeeMapper.toDto(employeeRepository.save(emp));
    }

    @Transactional
    public EmployeeDto transfer(Long id, EmployeeTransferRequest req) {
        Employee emp = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", id));
        Department dept = departmentRepository.findById(req.departmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department", req.departmentId()));
        emp.setDepartment(dept);
        return EmployeeMapper.toDto(employeeRepository.save(emp));
    }

    @Transactional
    public EmployeeDto updateStatus(Long id, EmployeeStatusRequest req) {
        Employee emp = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", id));
        try {
            EmploymentStatus es = EmploymentStatus.valueOf(req.status().toUpperCase());
            emp.setStatus(es);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Status must be ACTIVE, INACTIVE, or TERMINATED");
        }
        return EmployeeMapper.toDto(employeeRepository.save(emp));
    }
}
