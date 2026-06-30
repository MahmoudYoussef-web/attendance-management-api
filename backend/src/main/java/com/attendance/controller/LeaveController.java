package com.attendance.controller;

import com.attendance.dto.CreateLeaveRequest;
import com.attendance.dto.LeaveRequestDto;
import com.attendance.dto.ReviewLeaveRequest;
import com.attendance.entity.User;
import com.attendance.service.LeaveService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/leaves")
@RequiredArgsConstructor
/**
 * Leave request submission and review workflow.
 * <p>Submit: all authenticated roles. Review: SUPER_ADMIN/HR_MANAGER only.
 * Employees view only their own leaves; admins view all.
 * Layer: controller.</p>
 */
public class LeaveController {

    private final LeaveService leaveService;

    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'HR_MANAGER', 'EMPLOYEE')")
    @PostMapping
    public ResponseEntity<LeaveRequestDto> submit(
            @AuthenticationPrincipal User user,
            @RequestBody @Valid CreateLeaveRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(leaveService.submit(user.getId(), req));
    }

    @PreAuthorize("hasAnyAuthority('EMPLOYEE')")
    @GetMapping("/my")
    public ResponseEntity<List<LeaveRequestDto>> getMy(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(leaveService.getMyLeaves(user.getId()));
    }

    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'HR_MANAGER')")
    @GetMapping
    public ResponseEntity<Page<LeaveRequestDto>> getAll(Pageable pageable) {
        return ResponseEntity.ok(leaveService.getAll(pageable));
    }

    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'HR_MANAGER')")
    @PutMapping("/{id}/review")
    public ResponseEntity<LeaveRequestDto> review(
            @PathVariable Long id,
            @AuthenticationPrincipal User reviewer,
            @RequestBody @Valid ReviewLeaveRequest req) {
        return ResponseEntity.ok(leaveService.review(id, reviewer, req));
    }
}
