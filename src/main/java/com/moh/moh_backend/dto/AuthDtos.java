package com.moh.moh_backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTOs for authentication endpoints (register/login) without Spring Security.
 */
public class AuthDtos {
    public static class RegisterRequest {
        @NotBlank @Size(min = 3, max = 100)
        public String username;
        @NotBlank @Email
        public String email;
        @NotBlank @Size(min = 6, max = 128)
        public String password;
        @NotBlank
        public String role; // ADMIN/MIDWIFE/DOCTOR/MOTHER
    }

    public static class LoginRequest {
        @NotBlank
        public String usernameOrEmail;
        @NotBlank
        public String password;
    }

    public static class AuthResponse {
        public String token;
        public String username;
        public String role;
    }
}
