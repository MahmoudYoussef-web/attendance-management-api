package com.attendance.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * Request DTO — AI chat completion payload.
 * <p>Compatible with OpenAI-style API. Model, messages list, and optional generation params.
 * Layer: dto.</p>
 */
public record ChatRequest(
    @NotBlank String model,
    @NotNull List<Message> messages,
    Double temperature,
    Integer maxTokens
) {
    public record Message(
        @NotBlank String role,
        @NotBlank String content
    ) {}
}
