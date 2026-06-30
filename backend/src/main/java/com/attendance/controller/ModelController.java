package com.attendance.controller;

import com.attendance.config.NaraRouterConfig;
import com.attendance.dto.ChatRequest;
import com.attendance.dto.ChatResponse;
import com.attendance.service.NaraRouterService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/model")
@RequiredArgsConstructor
/**
 * AI model proxy endpoints.
 * <p>Exposes Nara API configuration and a chat completion proxy.
 * Layer: controller.</p>
 */
public class ModelController {

    private final NaraRouterService naraRouterService;
    private final NaraRouterConfig naraRouterConfig;

    @GetMapping("/config")
    public ResponseEntity<NaraRouterConfig> getConfig() {
        return ResponseEntity.ok(naraRouterConfig);
    }

    @PostMapping("/chat")
    public ResponseEntity<ChatResponse> chat(@RequestBody @Valid ChatRequest request) {
        var response = naraRouterService.chat(request);
        return ResponseEntity.ok(response);
    }
}
