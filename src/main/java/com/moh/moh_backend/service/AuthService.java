package com.moh.moh_backend.service;

import com.moh.moh_backend.dto.AuthDtos;
import com.moh.moh_backend.model.User;
import com.moh.moh_backend.model.UserRole;
import com.moh.moh_backend.repository.UserRepository;
import com.moh.moh_backend.util.JwtService;
import com.moh.moh_backend.util.PasswordHashService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Handles registration and login flows without Spring Security.
 * - Registration stores SHA-256 hashed password
 * - Login verifies SHA-256 hash
 * - Generates JWT containing username and role
 */
@Service
public class AuthService {
    private final UserRepository userRepo;
    private final PasswordHashService hashService;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepo, PasswordHashService hashService, JwtService jwtService) {
        this.userRepo = userRepo;
        this.hashService = hashService;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthDtos.AuthResponse register(AuthDtos.RegisterRequest req) {
        if (userRepo.existsByUsername(req.username)) {
            throw new IllegalArgumentException("Username already taken");

        }
        if (userRepo.existsByEmail(req.email)) {
            throw new IllegalArgumentException("Email already registered");
        }

        User user = new User();
        user.setUsername(req.username);
        user.setEmail(req.email);
        user.setPasswordHash(hashService.hashSha256(req.password));
        user.setRole(UserRole.valueOf(req.role));
        user.setIsActive(true);
        userRepo.save(user);

        Map<String, Object> claims = new HashMap<>();
        claims.put("role", user.getRole().name());
        String token = jwtService.generateToken(user.getUsername(), claims);

        AuthDtos.AuthResponse resp = new AuthDtos.AuthResponse();
        resp.token = token;
        resp.username = user.getUsername();
        resp.role = user.getRole().name();
        return resp;
    }

    public AuthDtos.AuthResponse login(AuthDtos.LoginRequest req) {
        Optional<User> byUsername = userRepo.findByEmail(req.Email);
        Optional<User> byEmail = byUsername.isPresent() ? Optional.empty() : userRepo.findByEmail(req.Email);
        User user = byUsername.orElseGet(() -> byEmail.orElseThrow(() -> new IllegalArgumentException("User not found")));

        String suppliedHash = hashService.hashSha256(req.password);
        if (!suppliedHash.equalsIgnoreCase(user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid credentials");
        }
        if (Boolean.FALSE.equals(user.getIsActive())) {
            throw new IllegalStateException("User is inactive");
        }

        Map<String, Object> claims = new HashMap<>();
        claims.put("role", user.getRole().name());
        String token = jwtService.generateToken(user.getUsername(), claims);

        AuthDtos.AuthResponse resp = new AuthDtos.AuthResponse();
        resp.token = token;
        resp.username = user.getUsername();
        resp.role = user.getRole().name();
        return resp;
    }
}
