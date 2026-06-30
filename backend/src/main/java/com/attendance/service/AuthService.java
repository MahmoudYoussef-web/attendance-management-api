package com.attendance.service;

import com.attendance.dto.*;
import com.attendance.entity.RefreshToken;
import com.attendance.entity.User;
import com.attendance.exception.*;
import com.attendance.repository.RefreshTokenRepository;
import com.attendance.repository.UserRepository;
import com.attendance.security.JwtTokenProvider;
import com.attendance.security.TokenBlacklistService;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
/**
 * Authentication and token management.
 * <p>Login validates credentials, enforces account lockout after 5 failed attempts,
 * and issues access+refresh token pair on success. Refresh implements token rotation
 * (old token revoked, new pair issued). Logout blacklists the JWT until its natural expiry.
 * Password changes require current password verification.
 * Layer: service.</p>
 */
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtProvider;
    private final TokenBlacklistService tokenBlacklist;
    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional
    public AuthLoginResponse login(String email, String rawPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));

        if (user.isLocked()) {
            throw new UnauthorizedException("Account is locked");
        }
        if (!user.isEnabled()) {
            throw new UnauthorizedException("Account is disabled");
        }
        if (!passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
            user.setFailedAttempts(user.getFailedAttempts() + 1);
            if (user.getFailedAttempts() >= 5) {
                user.setLocked(true);
            }
            userRepository.save(user);
            throw new UnauthorizedException("Invalid credentials");
        }
        user.setFailedAttempts(0);
        userRepository.save(user);

        String role = user.getRole().name();
        String access = jwtProvider.createAccessToken(user.getEmail(), role);
        String refresh = jwtProvider.createRefreshToken(user.getEmail(), role);

        persistRefreshToken(user, refresh);

        return new AuthLoginResponse(access, refresh, Instant.now().plusMillis(900_000).toEpochMilli());
    }

    @Transactional
    public AuthRefreshResponse refresh(String refreshToken) {
        Claims claims;
        try {
            claims = jwtProvider.parse(refreshToken).getBody();
        } catch (Exception e) {
            throw new UnauthorizedException("Invalid or expired refresh token");
        }

        String tokenHash = hashToken(refreshToken);
        RefreshToken stored = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new UnauthorizedException("Refresh token not found"));

        if (stored.isRevoked()) {
            throw new UnauthorizedException("Refresh token has been revoked");
        }
        if (stored.getExpiresAt().isBefore(Instant.now())) {
            throw new UnauthorizedException("Refresh token has expired");
        }

        stored.setRevoked(true);
        refreshTokenRepository.save(stored);

        String email = claims.getSubject();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("User not found"));

        if (user.isLocked()) {
            throw new UnauthorizedException("Account is locked");
        }
        if (!user.isEnabled()) {
            throw new UnauthorizedException("Account is disabled");
        }

        String role = user.getRole().name();
        String newAccess = jwtProvider.createAccessToken(email, role);
        String newRefresh = jwtProvider.createRefreshToken(email, role);

        persistRefreshToken(user, newRefresh);

        return new AuthRefreshResponse(newAccess, Instant.now().plusMillis(900_000).toEpochMilli());
    }

    public void logout(String accessToken) {
        var claims = jwtProvider.parse(accessToken).getBody();
        String jti = claims.getId();
        if (jti == null) return;
        long ttlSec = (claims.getExpiration().getTime() - System.currentTimeMillis()) / 1000;
        if (ttlSec > 0) {
            tokenBlacklist.blacklist(jti, ttlSec);
        }
    }

    @Transactional
    public void changePassword(String email, ChangePasswordRequest req) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", email));
        if (!passwordEncoder.matches(req.oldPassword(), user.getPasswordHash())) {
            throw new BadRequestException("Current password is incorrect");
        }
        user.setPasswordHash(passwordEncoder.encode(req.newPassword()));
        userRepository.save(user);
    }

    private void persistRefreshToken(User user, String rawToken) {
        RefreshToken token = RefreshToken.builder()
                .user(user)
                .tokenHash(hashToken(rawToken))
                .expiresAt(Instant.now().plus(7, ChronoUnit.DAYS))
                .build();
        refreshTokenRepository.save(token);
    }

    private String hashToken(String token) {
        try {
            var digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            var hex = new StringBuilder();
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }
}
