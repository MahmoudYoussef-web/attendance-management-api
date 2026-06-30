package com.attendance.mapper;

import com.attendance.dto.UserDto;
import com.attendance.entity.User;

/**
 * Maps between {@link User} entity and {@link UserDto}.
 * <p>Excludes sensitive fields (password hash). Exposes role, enable, and lock state.
 * Layer: mapper.</p>
 */
public class UserMapper {
    public static UserDto toDto(User user) {
        return new UserDto(
                user.getId(),
                user.getEmail(),
                user.getRole().name(),
                user.isEnabled(),
                user.isLocked()
        );
    }
}
