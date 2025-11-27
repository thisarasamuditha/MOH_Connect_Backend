package com.moh.moh_backend.service;

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Basic tests for SHA-256 hashing and JWT generation/validation.
 */
class AuthUtilTests {

    @Test
    void testSha256HashingDeterministicAndHexLength() {
        PasswordHashService hasher = new PasswordHashService();
        String h1 = hasher.hashSha256("password123");
        String h2 = hasher.hashSha256("password123");
        assertEquals(h1, h2, "Hash should be deterministic");
        assertEquals(64, h1.length(), "SHA-256 hex length should be 64");
        assertTrue(h1.matches("[0-9a-f]+"), "Hash should be lowercase hex");
    }

    @Test
    void testJwtGenerateAndValidate() {
        // 32 zero bytes as HS256 key
        byte[] keyBytes = new byte[32];
        String base64 = Base64.getEncoder().encodeToString(keyBytes);
        JwtService jwt = new JwtService(base64, 60000); // 60s TTL

        String token = jwt.generateToken("alice", Collections.singletonMap("role", "MOTHER"));
        assertNotNull(token);
        assertTrue(jwt.isValid(token));
    }
}
