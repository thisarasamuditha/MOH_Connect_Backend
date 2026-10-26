package com.moh.moh_backend.controller;

import com.moh.moh_backend.dto.AuthDtos;
import com.moh.moh_backend.service.AuthService;
import com.moh.moh_backend.service.PasswordService;
import jakarta.servlet.http.HttpServletRequest;
import com.moh.moh_backend.config.RequireRoles;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST endpoints for authentication without Spring Security.
 * - POST /auth/register (ADMIN only)
 * - POST /auth/login
 * - PUT  /auth/change-password (any logged-in user)
 * - POST /auth/forgot-password, POST /auth/reset-password (public)
 */
@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;
    private final PasswordService passwordService;

    public AuthController(AuthService authService, PasswordService passwordService) {
        this.authService = authService;
        this.passwordService = passwordService;
    }

    /** Staff accounts (ADMIN, DOCTOR, MIDWIFE) are created by an admin only; mothers via /mothers/register. */
    @PostMapping("/register")
    @RequireRoles("ADMIN")
    public ResponseEntity<AuthDtos.AuthResponse> register(@RequestBody @Valid AuthDtos.RegisterRequest req) {
        return ResponseEntity.ok(authService.register(req));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthDtos.AuthResponse> login(@RequestBody @Valid AuthDtos.LoginRequest req) {
        return ResponseEntity.ok(authService.login(req));
    }

    @PutMapping("/change-password")
    public ResponseEntity<AuthDtos.MessageResponse> changePassword(
            @RequestBody @Valid AuthDtos.ChangePasswordRequest req, HttpServletRequest request) {
        passwordService.changePassword((Integer) request.getAttribute("moh.userId"), req);
        return ResponseEntity.ok(new AuthDtos.MessageResponse("Password changed"));
    }

    /** Same answer whether or not the email is registered, so accounts cannot be discovered. */
    @PostMapping("/forgot-password")
    public ResponseEntity<AuthDtos.MessageResponse> forgotPassword(@RequestBody @Valid AuthDtos.ForgotPasswordRequest req) {
        passwordService.requestPasswordReset(req.email);
        return ResponseEntity.ok(new AuthDtos.MessageResponse(
                "If an account uses this email, a password reset link has been sent to it."));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<AuthDtos.MessageResponse> resetPassword(@RequestBody @Valid AuthDtos.ResetPasswordRequest req) {
        passwordService.resetPassword(req);
        return ResponseEntity.ok(new AuthDtos.MessageResponse("Password has been reset. You can now log in."));
    }

    /**
     * User management APIs (no Spring Security enforced in this project).
     */
    @GetMapping("/admins")
    @RequireRoles("ADMIN")
    public ResponseEntity<Iterable<AuthDtos.UserResponse>> getAllAdmins() {
        return ResponseEntity.ok(authService.getAllAdmins());
    }

    @GetMapping("/mothers")
    @RequireRoles("ADMIN")
    public ResponseEntity<Iterable<AuthDtos.UserResponse>> getAllMothers() {
        return ResponseEntity.ok(authService.getAllMothers());
    }

    @GetMapping("/midwives")
    @RequireRoles("ADMIN")
    public ResponseEntity<Iterable<AuthDtos.UserResponse>> getAllMidwives() {
        return ResponseEntity.ok(authService.getAllMidwives());
    }

    @GetMapping("/doctors")
    @RequireRoles({"ADMIN", "MIDWIFE", "DOCTOR"})
    public ResponseEntity<Iterable<AuthDtos.UserResponse>> getAllDoctors() {
        return ResponseEntity.ok(authService.getAllDoctors());
    }

    @GetMapping("/users/{id}")
    @RequireRoles("ADMIN")
    public ResponseEntity<AuthDtos.UserResponse> getUserById(@PathVariable Integer id) {
        return ResponseEntity.ok(authService.getUserById(id));
    }

    @PutMapping("/users/{id}")
    @RequireRoles("ADMIN")
    public ResponseEntity<AuthDtos.UserResponse> updateUser(
            @PathVariable Integer id,
            @RequestBody @Valid AuthDtos.UserUpdateRequest req) {
        return ResponseEntity.ok(authService.updateUser(id, req));
    }

    @DeleteMapping("/users/{id}")
    @RequireRoles("ADMIN")
    public ResponseEntity<Void> deleteUser(@PathVariable Integer id) {
        authService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
