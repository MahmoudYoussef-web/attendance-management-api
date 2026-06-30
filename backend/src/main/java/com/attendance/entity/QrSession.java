package com.attendance.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "qr_sessions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
/**
 * QR code session — maps to {@code qr_sessions} table.
 * <p>Each session represents a time-limited QR code that employees scan
 * to check in. Session ID is HMAC-signed and stored in Redis with a matching TTL.
 * Layer: entity.</p>
 */
public class QrSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "session_id", nullable = false, unique = true, length = 36)
    private String sessionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}
