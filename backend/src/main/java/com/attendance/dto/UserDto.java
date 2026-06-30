package com.attendance.dto;

/**
 * Data Transfer Object used when returning user information to the client.
 * Only expose fields that are safe to share (no password, no internal flags).
 */
public record UserDto(Long id, String email, String role, boolean enabled, boolean locked) {};
