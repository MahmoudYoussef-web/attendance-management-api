package com.attendance.service;

import com.attendance.config.QrCodeConfig;
import com.attendance.dto.QrSessionResponse;
import com.attendance.dto.QrSessionSummaryDto;
import com.attendance.entity.QrSession;
import com.attendance.entity.User;
import com.attendance.exception.BadRequestException;
import com.attendance.repository.QrSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Base64;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
/**
 * QR code session lifecycle — create, verify, deactivate.
 * <p>Creates time-limited sessions with HMAC-SHA256 signing for tamper-proof QR codes.
 * Uses an in-memory ConcurrentHashMap as a lightweight session cache (TTL matches session expiry).
 * Design choice: in-memory cache over Redis for simpler deployment; upgrade to Redis if
 * horizontal scaling is needed. MarkUsed prevents duplicate check-ins per session.
 * Layer: service.</p>
 */
public class QrService {

    private static final String SESSION_KEY = "qr:session:";
    private static final String USED_KEY = "qr:used:";

    private final QrCodeConfig config;
    private final QrSessionRepository qrSessionRepository;
    private final ConcurrentHashMap<String, String> cache = new ConcurrentHashMap<>();
    private final ScheduledExecutorService cleaner = Executors.newSingleThreadScheduledExecutor();

    @Transactional
    public QrSessionResponse createSession(User admin) {
        String sessionId = UUID.randomUUID().toString();
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(config.getSessionTtlMinutes());

        QrSession session = QrSession.builder()
                .sessionId(sessionId)
                .createdBy(admin)
                .expiresAt(expiresAt)
                .build();
        qrSessionRepository.save(session);

        long expiresAtEpoch = expiresAt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        long ttlSeconds = config.getSessionTtlMinutes() * 60;
        cache.put(SESSION_KEY + sessionId, String.valueOf(expiresAtEpoch));
        cleaner.schedule(() -> cache.remove(SESSION_KEY + sessionId), ttlSeconds, TimeUnit.SECONDS);

        String payload = sessionId + "|" + LocalDate.now() + "|" + expiresAtEpoch;
        String signature = sign(payload);

        return new QrSessionResponse(
                session.getId(), sessionId, admin.getId(), expiresAt,
                true, session.getCreatedAt(), payload, signature
        );
    }

    public String[] validateAndParse(String qrPayload, String qrSignature) {
        if (!verify(qrPayload, qrSignature)) {
            throw new BadRequestException("Invalid QR signature");
        }
        String[] parts = qrPayload.split("\\|");
        if (parts.length != 3) {
            throw new BadRequestException("Invalid QR payload format");
        }
        String sessionId = parts[0];
        long expiresAtEpoch = Long.parseLong(parts[2]);

        if (System.currentTimeMillis() > expiresAtEpoch) {
            throw new BadRequestException("QR session has expired");
        }
        if (!cache.containsKey(SESSION_KEY + sessionId)) {
            throw new BadRequestException("QR session is no longer valid");
        }
        return parts;
    }

    public boolean hasUsed(String sessionId, Long employeeId) {
        return cache.containsKey(USED_KEY + sessionId + ":" + employeeId);
    }

    public void markUsed(String sessionId, Long employeeId) {
        long ttlSeconds = config.getSessionTtlMinutes() * 60;
        cache.put(USED_KEY + sessionId + ":" + employeeId, "true");
        cleaner.schedule(() -> cache.remove(USED_KEY + sessionId + ":" + employeeId), ttlSeconds, TimeUnit.SECONDS);
    }

    @Transactional(readOnly = true)
    public List<QrSessionSummaryDto> getSessions() {
        return qrSessionRepository.findAllByOrderByCreatedAtDesc()
                .stream().map(s -> new QrSessionSummaryDto(
                        s.getId(), s.getSessionId(), s.getCreatedBy().getEmail(),
                        s.getCreatedAt(), s.getExpiresAt(),
                        s.getIsActive() != null && s.getIsActive()
                )).toList();
    }

    @Transactional
    public void deactivateSession(String sessionId) {
        QrSession session = qrSessionRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new BadRequestException("Session not found"));
        session.setIsActive(false);
        qrSessionRepository.save(session);
        cache.remove(SESSION_KEY + sessionId);
        cache.keySet().removeIf(k -> k.startsWith(USED_KEY + sessionId + ":"));
    }

    private String sign(String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec key = new SecretKeySpec(
                    config.getHmacSecret().getBytes(), "HmacSHA256");
            mac.init(key);
            return Base64.getEncoder().encodeToString(mac.doFinal(data.getBytes()));
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("HMAC error", e);
        }
    }

    private boolean verify(String data, String signature) {
        return sign(data).equals(signature);
    }
}
