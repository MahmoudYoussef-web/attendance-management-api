package com.attendance.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown on authentication failures: invalid credentials, locked/disabled account,
 * expired or revoked refresh token.
 * <p>Maps to HTTP 401.
 * Layer: exception.</p>
 */
public class UnauthorizedException extends ApiException {
    public UnauthorizedException(String message) { super(message, HttpStatus.UNAUTHORIZED); }
}
