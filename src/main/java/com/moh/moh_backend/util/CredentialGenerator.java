package com.moh.moh_backend.util;

import java.security.SecureRandom;

/**
 * Utility for generating secure credentials
 */
public class CredentialGenerator {

    private static final SecureRandom random = new SecureRandom();
    private static final String CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final String PASSWORD_CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789!@#$%";

    /**
     * Generate random username (8 characters)
     */
    public static String generateUsername() {
        StringBuilder username = new StringBuilder("mother_");
        for (int i = 0; i < 8; i++) {
            username.append(CHARS.charAt(random.nextInt(CHARS.length())));
        }
        return username.toString();
    }

    /**
     * Generate random password (12 characters, mixed case, numbers, symbols)
     */
    public static String generatePassword() {
        StringBuilder password = new StringBuilder();
        for (int i = 0; i < 12; i++) {
            password.append(PASSWORD_CHARS.charAt(random.nextInt(PASSWORD_CHARS.length())));
        }
        return password.toString();
    }
}
