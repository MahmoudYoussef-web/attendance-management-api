package com.attendance.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "qr")
@Getter
@Setter
/**
 * QR code security and session configuration.
 * <p>hmacSecret is used by QrService to HMAC-SHA256 sign QR payloads (tamper protection).
 * sessionTtlMinutes controls both the DB session expiry and the in-memory cache TTL.
 * Layer: config.</p>
 */
public class QrCodeConfig {

    private String hmacSecret = "change-me-qr-hmac-secret-32-chars-min!!";
    private long sessionTtlMinutes = 720;
}
