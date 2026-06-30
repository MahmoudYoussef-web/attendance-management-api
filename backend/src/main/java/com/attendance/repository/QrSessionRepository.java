package com.attendance.repository;

import com.attendance.entity.QrSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Data access for {@link QrSession}.
 * <p>Lookup by sessionId (UUID) for QR check-in verification.
 * Layer: repository.</p>
 */
public interface QrSessionRepository extends JpaRepository<QrSession, Long> {
    Optional<QrSession> findBySessionId(String sessionId);
    List<QrSession> findAllByOrderByCreatedAtDesc();
}
