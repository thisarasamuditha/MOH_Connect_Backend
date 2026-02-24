package com.moh.moh_backend.repository;

import com.moh.moh_backend.model.User;
import com.moh.moh_backend.model.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for USER table operations.
 */
public interface UserRepository extends JpaRepository<User, Integer> {

//  SELECT * FROM user WHERE email = ?; this works under the hood
    Optional<User> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    List<User> findByRole(UserRole role);
}
