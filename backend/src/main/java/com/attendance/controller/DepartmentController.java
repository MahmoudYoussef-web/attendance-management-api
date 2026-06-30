package com.attendance.controller;

import com.attendance.dto.*;
import com.attendance.service.DepartmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/departments")
@RequiredArgsConstructor
/**
 * Department CRUD with role-based access.
 * <p>Create/update/delete: SUPER_ADMIN only. Read: SUPER_ADMIN or HR_MANAGER.
 * Layer: controller.</p>
 */
public class DepartmentController {

    private final DepartmentService departmentService;

    @PreAuthorize("hasAuthority('SUPER_ADMIN')")
    @PostMapping
    public ResponseEntity<DepartmentDto> create(@RequestBody @Valid CreateDepartmentRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(departmentService.create(req));
    }

    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'HR_MANAGER')")
    @GetMapping
    public ResponseEntity<List<DepartmentDto>> getAll() {
        return ResponseEntity.ok(departmentService.getAll());
    }

    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'HR_MANAGER')")
    @GetMapping("/{id}")
    public ResponseEntity<DepartmentDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(departmentService.getById(id));
    }

    @PreAuthorize("hasAuthority('SUPER_ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<DepartmentDto> update(@PathVariable Long id, @RequestBody @Valid UpdateDepartmentRequest req) {
        return ResponseEntity.ok(departmentService.update(id, req));
    }

    @PreAuthorize("hasAuthority('SUPER_ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        departmentService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
