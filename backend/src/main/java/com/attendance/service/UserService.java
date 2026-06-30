package com.attendance.service;

import com.attendance.dto.*;
import com.attendance.entity.User;
import com.attendance.exception.BadRequestException;
import com.attendance.exception.ResourceNotFoundException;
import com.attendance.mapper.UserMapper;
import com.attendance.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
/**
 * User account management (admin).
 * <p>Handles email uniqueness, password hashing at creation, and field-level updates
 * for role, enable/disable, and lock state.
 * Layer: service.</p>
 */
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;


    @Transactional
    public UserDto createUser(UserCreateRequest req) {
        if (userRepository.findByEmail(req.email()).isPresent()) {
            throw new BadRequestException("Email already in use");
        }
        User user = User.builder()
                .email(req.email())
                .passwordHash(passwordEncoder.encode(req.rawPassword()))
                .role(com.attendance.entity.Role.valueOf(req.role()))
                .enabled(true)
                .locked(false)
                .failedAttempts(0)
                .build();
        User saved = userRepository.save(user);
        return UserMapper.toDto(saved);
    }

    @Transactional(readOnly = true)
    public List<UserDto> getAll() {
        return userRepository.findAll().stream()
                .map(UserMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UserDto getById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
        return UserMapper.toDto(user);
    }

    @Transactional
    public UserDto updateUser(Long id, UserUpdateRequest req) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
        if (req.email() != null && !req.email().equals(user.getEmail())) {
            if (userRepository.findByEmail(req.email()).isPresent()) {
                throw new BadRequestException("Email already in use");
            }
            user.setEmail(req.email());
        }
        if (req.role() != null) user.setRole(com.attendance.entity.Role.valueOf(req.role()));
        if (req.enabled() != null) user.setEnabled(req.enabled());
        if (req.locked() != null) user.setLocked(req.locked());
        User saved = userRepository.save(user);
        return UserMapper.toDto(saved);
    }

    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User", id);
        }
        userRepository.deleteById(id);
    }
}
