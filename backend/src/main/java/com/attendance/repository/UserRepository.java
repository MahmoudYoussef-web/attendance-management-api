package com.attendance.repository;

import com.attendance.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

/**
 * Data access for {@link User}.
 * <p>Email-based lookup for authentication (login).
 * Layer: repository.</p>
 */
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
}
