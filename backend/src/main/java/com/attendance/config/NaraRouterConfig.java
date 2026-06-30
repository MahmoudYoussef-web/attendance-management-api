package com.attendance.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "nara-router")
@Getter
@Setter
/**
 * Nara AI router configuration.
 * <p>Points to the external Nara API endpoint for chat completions.
 * apiKey should be set via environment variable (nara-router.api-key).
 * Layer: config.</p>
 */
public class NaraRouterConfig {

    private String baseUrl = "https://router.bynara.id/v1";
    private String apiKey = "";
    private String model = "mimo-v2.5-free";
}
