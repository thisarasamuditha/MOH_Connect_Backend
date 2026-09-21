package com.moh.moh_backend.util;

import org.springframework.stereotype.Service;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Provides SHA-256 hashing for passwords. This is a simple hash without salt
 * as requested. For production, prefer salted hashes (e.g., BCrypt/Argon2).
 */
@Service
public class PasswordHashService {
    private final BCryptPasswordEncoder bcrypt = new BCryptPasswordEncoder();

    public String hashPassword(String plain) {
        return bcrypt.encode(plain);
    }

    public boolean matches(String plain, String storedHash) {
        if (storedHash == null || storedHash.isBlank()) return false;
        if (storedHash.startsWith("$2a$") || storedHash.startsWith("$2b$") || storedHash.startsWith("$2y$")) {
            return bcrypt.matches(plain, storedHash);
        }
        return hashSha256(plain).equalsIgnoreCase(storedHash);
    }

    /** Hash plain text password with SHA-256 and return hex string. */
    public String hashSha256(String plain) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(plain.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
