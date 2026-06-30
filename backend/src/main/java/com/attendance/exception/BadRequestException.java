package com.attendance.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when the client sends an invalid request (validation, duplicate, overlap, etc.).
 * <p>Maps to HTTP 400. Used for business logic validation failures.
 * Layer: exception.</p>
 */
public class BadRequestException extends ApiException {
    public BadRequestException(String message) { super(message, HttpStatus.BAD_REQUEST); }
}
