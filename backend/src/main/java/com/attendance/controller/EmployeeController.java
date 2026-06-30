package com.attendance.controller;

import com.attendance.dto.*;
import com.attendance.service.EmployeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
/**
 * Employee profile CRUD, transfer, and status management.
 * <p>All endpoints require SUPER_ADMIN or HR_MANAGER.
 * Layer: controller.</p>
 */
public class EmployeeController {

    private final EmployeeService employeeService;

    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'HR_MANAGER')")
    @PostMapping
    public ResponseEntity<EmployeeDto> create(@RequestBody @Valid CreateEmployeeRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(employeeService.create(req));
    }

    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'HR_MANAGER')")
    @GetMapping
    public ResponseEntity<Page<EmployeeDto>> getAll(Pageable pageable) {
        return ResponseEntity.ok(employeeService.getAll(pageable));
    }

    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'HR_MANAGER')")
    @GetMapping("/{id}")
    public ResponseEntity<EmployeeDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(employeeService.getById(id));
    }

    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'HR_MANAGER')")
    @PutMapping("/{id}")
    public ResponseEntity<EmployeeDto> update(@PathVariable Long id, @RequestBody @Valid UpdateEmployeeRequest req) {
        return ResponseEntity.ok(employeeService.update(id, req));
    }

    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'HR_MANAGER')")
    @PutMapping("/{id}/transfer")
    public ResponseEntity<EmployeeDto> transfer(@PathVariable Long id, @RequestBody @Valid EmployeeTransferRequest req) {
        return ResponseEntity.ok(employeeService.transfer(id, req));
    }

    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'HR_MANAGER')")
    @PutMapping("/{id}/status")
    public ResponseEntity<EmployeeDto> updateStatus(@PathVariable Long id, @RequestBody @Valid EmployeeStatusRequest req) {
        return ResponseEntity.ok(employeeService.updateStatus(id, req));
    }
}
