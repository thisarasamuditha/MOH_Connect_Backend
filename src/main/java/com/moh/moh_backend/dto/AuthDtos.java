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
        @NotBlank @Size(min = 6, max = 30)
        public String password;
        @NotBlank
        public String role; // ADMIN/MIDWIFE/DOCTOR/MOTHER

        // Doctor-specific fields
        public DoctorDetails doctorDetails;
        // Midwife-specific fields
        public MidwifeDetails midwifeDetails;
        // Mother-specific fields
        public String wifeNic;

        public static class DoctorDetails {
            public String name;
            public String specialization;
            public String contactNumber;
            public String email;
            public String licenseNumber;
        }
        public static class MidwifeDetails {
            public String name;
            public Integer phmAreaId;
            public String contactNumber;
            public String email;
            public String assignmentDate;
            public String qualifications;
        }
    }

    public static class LoginRequest {
        @NotBlank
        public String email;
        @NotBlank
        public String password;
    }

    public static class AuthResponse {
        public String token;
        public Integer userId;     // users table PK
        public String username;
        public String role;
        public String name;        // Full name (from Midwife/Doctor profile)
        public Integer staffId;    // midwife_id or doctor_id
        public Integer phmAreaId;  // Midwife's PHM area ID
        public String phmAreaName; // Midwife's PHM area name
    }

    /**
     * Safe user representation (never exposes passwordHash).
     */
    public static class UserResponse {
        public Integer userId;
        public String username;
        public String email;
        public String role;
        public Boolean isActive;
        public String createdAt;
        public String lastLogin;
    }

    /**
     * User profile update request.
     * All fields are optional; at least one must be provided.
     */
    public static class UserUpdateRequest {
        @Size(min = 3, max = 100)
        public String username;

        @Email
        public String email;

        @Size(min = 6, max = 128)
        public String password;

        public Boolean isActive;
    }
}
