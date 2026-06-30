package com.attendance.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when a requested entity does not exist.
 * <p>Maps to HTTP 404. Accepts entity name and identifier for a descriptive message.
 * Layer: exception.</p>
 */
public class ResourceNotFoundException extends ApiException {
    public ResourceNotFoundException(String entity, Object identifier) {
        super(entity + " not found with id " + identifier, HttpStatus.NOT_FOUND);
    }
}
