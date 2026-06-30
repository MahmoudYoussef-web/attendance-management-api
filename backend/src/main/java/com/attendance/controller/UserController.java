package com.attendance.controller;

import com.attendance.dto.*;
import com.attendance.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * User account CRUD — SUPER_ADMIN only.
 * <p>Manages authentication accounts: email, role, enable/lock state.
 * Layer: controller.</p>
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PreAuthorize("hasAuthority('SUPER_ADMIN')")
    @PostMapping
    public ResponseEntity<UserDto> create(@RequestBody @Valid UserCreateRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.createUser(req));
    }

    @PreAuthorize("hasAuthority('SUPER_ADMIN')")
    @GetMapping
    public ResponseEntity<List<UserDto>> list() {
        return ResponseEntity.ok(userService.getAll());
    }

    @PreAuthorize("hasAuthority('SUPER_ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<UserDto> get(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getById(id));
    }

    @PreAuthorize("hasAuthority('SUPER_ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<UserDto> update(@PathVariable Long id, @RequestBody @Valid UserUpdateRequest req) {
        return ResponseEntity.ok(userService.updateUser(id, req));
    }

    @PreAuthorize("hasAuthority('SUPER_ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
