package com.moh.moh_backend.controller;

import com.moh.moh_backend.dto.AuthDtos;
import com.moh.moh_backend.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST endpoints for authentication without Spring Security.
 * - POST /api/auth/register
 * - POST /api/auth/login
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthDtos.AuthResponse> register(@RequestBody @Valid AuthDtos.RegisterRequest req) {
        return ResponseEntity.ok(authService.register(req));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthDtos.AuthResponse> login(@RequestBody @Valid AuthDtos.LoginRequest req) {
        return ResponseEntity.ok(authService.login(req));
    }

    /**
     * User management APIs (no Spring Security enforced in this project).
     */
    @GetMapping("/users")
    public ResponseEntity<Iterable<AuthDtos.UserResponse>> getAllUsers() {
        return ResponseEntity.ok(authService.getAllUsers());
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<AuthDtos.UserResponse> getUserById(@PathVariable Integer id) {
        return ResponseEntity.ok(authService.getUserById(id));
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<AuthDtos.UserResponse> updateUser(
            @PathVariable Integer id,
            @RequestBody @Valid AuthDtos.UserUpdateRequest req) {
        return ResponseEntity.ok(authService.updateUser(id, req));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Integer id) {
        authService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
