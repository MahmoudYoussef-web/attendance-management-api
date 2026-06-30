package com.attendance.repository;

import com.attendance.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Data access for {@link RefreshToken}.
 * <p>Lookup by token hash during refresh token rotation.
 * Layer: repository.</p>
 */
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByTokenHash(String tokenHash);
}
