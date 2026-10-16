package com.moh.moh_backend.service;

import com.moh.moh_backend.dto.AuthDtos;
import com.moh.moh_backend.model.PasswordResetToken;
import com.moh.moh_backend.model.User;
import com.moh.moh_backend.repository.PasswordResetTokenRepository;
import com.moh.moh_backend.repository.UserRepository;
import com.moh.moh_backend.util.EmailService;
import com.moh.moh_backend.util.PasswordHashService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;

/**
 * Password self-service: change own password, and "forgot password" by a one-time emailed link.
 */
@Service
public class PasswordService {
    static final int RESET_LINK_VALID_MINUTES = 30;
    static final String INVALID_LINK_MESSAGE =
            "This reset link is invalid or has expired. Please request a new one.";

    private static final SecureRandom RANDOM = new SecureRandom();

    private final UserRepository userRepo;
    private final PasswordResetTokenRepository tokenRepo;
    private final PasswordHashService hashService;
    private final EmailService emailService;
    private final String frontendBaseUrl;

    public PasswordService(UserRepository userRepo, PasswordResetTokenRepository tokenRepo,
                           PasswordHashService hashService, EmailService emailService,
                           @Value("${app.frontend.base-url:http://localhost:3000}") String frontendBaseUrl) {
        this.userRepo = userRepo;
        this.tokenRepo = tokenRepo;
        this.hashService = hashService;
        this.emailService = emailService;
        this.frontendBaseUrl = frontendBaseUrl.replaceAll("/+$", "");
    }

    @Transactional
    public void changePassword(Integer userId, AuthDtos.ChangePasswordRequest req) {
        if (userId == null) {
            throw new IllegalArgumentException("Not logged in");
        }
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        if (Boolean.FALSE.equals(user.getIsActive())) {
            throw new IllegalStateException("User is inactive");
        }
        if (!hashService.matches(req.currentPassword, user.getPasswordHash())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }
        validateNewPassword(req.newPassword);
        if (hashService.matches(req.newPassword, user.getPasswordHash())) {
            throw new IllegalArgumentException("New password must be different from the current password");
        }
        user.setPasswordHash(hashService.hashPassword(req.newPassword));
        userRepo.save(user);
    }

    /**
     * Emails a reset link if the address belongs to an active user. Always finishes silently,
     * so the caller cannot find out which emails are registered.
     */
    @Transactional
    public void requestPasswordReset(String email) {
        String address = email == null ? "" : email.trim();
        Optional<User> userOpt = userRepo.findByEmail(address);
        if (userOpt.isEmpty() || Boolean.FALSE.equals(userOpt.get().getIsActive())) {
            return;
        }
        User user = userOpt.get();
        LocalDateTime now = LocalDateTime.now();
        tokenRepo.invalidateOpenTokens(user.getUserId(), now);

        String token = newToken();
        PasswordResetToken reset = new PasswordResetToken();
        reset.setUser(user);
        reset.setTokenHash(hashService.hashSha256(token));
        reset.setExpiresAt(now.plusMinutes(RESET_LINK_VALID_MINUTES));
        tokenRepo.save(reset);

        String link = frontendBaseUrl + "/reset-password?token=" + token;
        emailService.sendPasswordResetLink(user.getEmail(), user.getUsername(), link, RESET_LINK_VALID_MINUTES);
    }

    @Transactional
    public void resetPassword(AuthDtos.ResetPasswordRequest req) {
        String token = req.token == null ? "" : req.token.trim();
        PasswordResetToken reset = tokenRepo.findByTokenHash(hashService.hashSha256(token))
                .orElseThrow(() -> new IllegalArgumentException(INVALID_LINK_MESSAGE));
        LocalDateTime now = LocalDateTime.now();
        User user = reset.getUser();
        if (reset.getUsedAt() != null || !now.isBefore(reset.getExpiresAt())
                || Boolean.FALSE.equals(user.getIsActive())) {
            throw new IllegalArgumentException(INVALID_LINK_MESSAGE);
        }
        validateNewPassword(req.newPassword);

        user.setPasswordHash(hashService.hashPassword(req.newPassword));
        userRepo.save(user);
        reset.setUsedAt(now);
        tokenRepo.save(reset);
        tokenRepo.invalidateOpenTokens(user.getUserId(), now);
    }

    /** At least 8 characters with at least one letter and one digit. */
    static void validateNewPassword(String password) {
        if (password == null || password.length() < 8) {
            throw new IllegalArgumentException("New password must be at least 8 characters");
        }
        if (password.length() > 128) {
            throw new IllegalArgumentException("New password must be at most 128 characters");
        }
        if (!password.matches(".*[A-Za-z].*") || !password.matches(".*\\d.*")) {
            throw new IllegalArgumentException("New password must contain at least one letter and one number");
        }
    }

    private static String newToken() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
