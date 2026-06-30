package com.attendance.service;

import com.attendance.config.NaraRouterConfig;
import com.attendance.dto.ChatRequest;
import com.attendance.dto.ChatResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
/**
 * AI chat proxy — forwards requests to the configured Nara API endpoint.
 * <p>Passthrough service: adds the Bearer API key and delegates to the upstream
 * /chat/completions endpoint. Response is returned as-is.
 * Layer: service.</p>
 */
public class NaraRouterService {

    private final NaraRouterConfig config;
    private final RestTemplate restTemplate;

    public ChatResponse chat(ChatRequest request) {
        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(config.getApiKey());

        var entity = new HttpEntity<>(request, headers);
        return restTemplate.postForObject(
            config.getBaseUrl() + "/chat/completions",
            entity,
            ChatResponse.class
        );
    }
}
