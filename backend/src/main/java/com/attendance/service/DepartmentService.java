package com.attendance.service;

import com.attendance.dto.*;
import com.attendance.entity.Department;
import com.attendance.exception.BadRequestException;
import com.attendance.exception.ResourceNotFoundException;
import com.attendance.mapper.DepartmentMapper;
import com.attendance.repository.DepartmentRepository;
import com.attendance.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
/**
 * Department CRUD with duplicate-name prevention.
 * <p>Deletion is blocked if employees are still assigned to the department.
 * Layer: service.</p>
 */
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final EmployeeRepository employeeRepository;

    @Transactional
    public DepartmentDto create(CreateDepartmentRequest req) {
        if (departmentRepository.findByName(req.name()).isPresent()) {
            throw new BadRequestException("Department name already exists");
        }
        Department dept = Department.builder()
                .name(req.name())
                .description(req.description())
                .build();
        return DepartmentMapper.toDto(departmentRepository.save(dept));
    }

    @Transactional(readOnly = true)
    public List<DepartmentDto> getAll() {
        return departmentRepository.findAll().stream()
                .map(DepartmentMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public DepartmentDto getById(Long id) {
        Department dept = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department", id));
        return DepartmentMapper.toDto(dept);
    }

    @Transactional
    public DepartmentDto update(Long id, UpdateDepartmentRequest req) {
        Department dept = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department", id));
        if (req.name() != null && !req.name().equals(dept.getName())) {
            if (departmentRepository.findByName(req.name()).isPresent()) {
                throw new BadRequestException("Department name already exists");
            }
            dept.setName(req.name());
        }
        if (req.description() != null) {
            dept.setDescription(req.description());
        }
        return DepartmentMapper.toDto(departmentRepository.save(dept));
    }

    @Transactional
    public void delete(Long id) {
        Department dept = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department", id));
        if (employeeRepository.existsByDepartmentId(id)) {
            throw new BadRequestException("Cannot delete department with active employees");
        }
        departmentRepository.delete(dept);
    }
}
