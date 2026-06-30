package com.attendance.controller;

import com.attendance.dto.*;
import com.attendance.service.WorkScheduleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/schedules")
@RequiredArgsConstructor
/**
 * Work schedule CRUD.
 * <p>Most operations require SUPER_ADMIN or HR_MANAGER.
 * Delete is SUPER_ADMIN only.
 * Layer: controller.</p>
 */
public class WorkScheduleController {

    private final WorkScheduleService workScheduleService;

    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'HR_MANAGER')")
    @PostMapping
    public ResponseEntity<WorkScheduleDto> create(@RequestBody @Valid CreateWorkScheduleRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(workScheduleService.create(req));
    }

    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'HR_MANAGER')")
    @GetMapping
    public ResponseEntity<List<WorkScheduleDto>> getAll() {
        return ResponseEntity.ok(workScheduleService.getAll());
    }

    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'HR_MANAGER')")
    @GetMapping("/{id}")
    public ResponseEntity<WorkScheduleDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(workScheduleService.getById(id));
    }

    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'HR_MANAGER')")
    @PutMapping("/{id}")
    public ResponseEntity<WorkScheduleDto> update(@PathVariable Long id, @RequestBody @Valid UpdateWorkScheduleRequest req) {
        return ResponseEntity.ok(workScheduleService.update(id, req));
    }

    @PreAuthorize("hasAuthority('SUPER_ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        workScheduleService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
