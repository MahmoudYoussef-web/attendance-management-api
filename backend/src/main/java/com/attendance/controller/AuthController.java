package com.attendance.controller;

import com.attendance.dto.*;
import com.attendance.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Public authentication endpoints — login, refresh, logout, change-password.
 * <p>No auth required for login/refresh. Logout and change-password require authentication.
 * Layer: controller.</p>
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthLoginResponse> login(@RequestBody @Valid AuthLoginRequest req) {
        return ResponseEntity.ok(authService.login(req.email(), req.password()));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthRefreshResponse> refresh(@RequestBody @Valid AuthRefreshRequest req) {
        return ResponseEntity.ok(authService.refresh(req.refreshToken()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader("Authorization") String bearer) {
        String token = bearer.replace("Bearer ", "");
        authService.logout(token);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/change-password")
    public ResponseEntity<Void> changePassword(@AuthenticationPrincipal UserDetails userDetails,
                                               @RequestBody @Valid ChangePasswordRequest req) {
        authService.changePassword(userDetails.getUsername(), req);
        return ResponseEntity.ok().build();
    }
}
