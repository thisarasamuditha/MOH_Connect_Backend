package com.moh.moh_backend.util;

import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Provides SHA-256 hashing for passwords. This is a simple hash without salt
 * as requested. For production, prefer salted hashes (e.g., BCrypt/Argon2).
 */
@Service
public class PasswordHashService {
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
