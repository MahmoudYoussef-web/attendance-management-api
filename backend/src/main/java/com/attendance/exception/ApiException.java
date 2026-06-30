package com.attendance.exception;

import org.springframework.http.HttpStatus;

/**
 * Base class for application‑specific exceptions that carry an HTTP status.
 */
public abstract class ApiException extends RuntimeException {
    private final HttpStatus status;
    public ApiException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }
    public HttpStatus getStatus() { return status; }
}
