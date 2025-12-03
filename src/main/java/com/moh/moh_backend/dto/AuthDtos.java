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
            public Long phmAreaId;
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
        public String username;
        public String role;
    }
}
