package com.moh.moh_backend.service;

import com.moh.moh_backend.config.JwtAuthenticationFilter;
import com.moh.moh_backend.dto.AuthDtos;
import com.moh.moh_backend.model.PasswordResetToken;
import com.moh.moh_backend.model.User;
import com.moh.moh_backend.repository.PasswordResetTokenRepository;
import com.moh.moh_backend.repository.UserRepository;
import com.moh.moh_backend.util.EmailService;
import com.moh.moh_backend.util.JwtService;
import com.moh.moh_backend.util.PasswordHashService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Change password and "forgot password" (emailed one-time link).
 */
class PasswordServiceTests {

    private final UserRepository userRepo = mock(UserRepository.class);
    private final PasswordResetTokenRepository tokenRepo = mock(PasswordResetTokenRepository.class);
    private final EmailService emailService = mock(EmailService.class);
    private final PasswordHashService hashService = new PasswordHashService();
    private final PasswordService service = new PasswordService(userRepo, tokenRepo, hashService,
            emailService, "https://moh.example.lk/");

    private User user(String password) {
        User u = new User();
        u.setUserId(7);
        u.setUsername("midwife_anne");
        u.setEmail("anne@example.com");
        u.setPasswordHash(hashService.hashPassword(password));
        u.setIsActive(true);
        return u;
    }

    private static AuthDtos.ChangePasswordRequest change(String current, String next) {
        AuthDtos.ChangePasswordRequest req = new AuthDtos.ChangePasswordRequest();
        req.currentPassword = current;
        req.newPassword = next;
        return req;
    }

    private static AuthDtos.ResetPasswordRequest reset(String token, String next) {
        AuthDtos.ResetPasswordRequest req = new AuthDtos.ResetPasswordRequest();
        req.token = token;
        req.newPassword = next;
        return req;
    }

    private PasswordResetToken storedToken(User u, String token, LocalDateTime expiresAt, LocalDateTime usedAt) {
        PasswordResetToken t = new PasswordResetToken();
        t.setUser(u);
        t.setTokenHash(hashService.hashSha256(token));
        t.setExpiresAt(expiresAt);
        t.setUsedAt(usedAt);
        when(tokenRepo.findByTokenHash(hashService.hashSha256(token))).thenReturn(Optional.of(t));
        return t;
    }

    // ---- change password ----

    @Test
    void changePasswordStoresNewHash() {
        User u = user("OldPass123");
        when(userRepo.findById(7)).thenReturn(Optional.of(u));

        service.changePassword(7, change("OldPass123", "NewPass456"));

        assertTrue(hashService.matches("NewPass456", u.getPasswordHash()));
        verify(userRepo).save(u);
    }

    @Test
    void changePasswordRejectsWrongCurrentPassword() {
        when(userRepo.findById(7)).thenReturn(Optional.of(user("OldPass123")));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.changePassword(7, change("wrong", "NewPass456")));
        assertEquals("Current password is incorrect", ex.getMessage());
        verify(userRepo, never()).save(any());
    }

    @Test
    void changePasswordRejectsSamePasswordAndWeakPassword() {
        when(userRepo.findById(7)).thenReturn(Optional.of(user("OldPass123")));

        assertThrows(IllegalArgumentException.class,
                () -> service.changePassword(7, change("OldPass123", "OldPass123")));
        assertThrows(IllegalArgumentException.class,
                () -> service.changePassword(7, change("OldPass123", "onlyletters")));
        assertThrows(IllegalArgumentException.class,
                () -> service.changePassword(7, change("OldPass123", "short1")));
        verify(userRepo, never()).save(any());
    }

    @Test
    void changePasswordNeedsLoggedInUser() {
        assertThrows(IllegalArgumentException.class,
                () -> service.changePassword(null, change("OldPass123", "NewPass456")));
    }

    // ---- forgot password ----

    @Test
    void forgotPasswordEmailsLinkAndStoresOnlyHash() {
        User u = user("OldPass123");
        when(userRepo.findByEmail("anne@example.com")).thenReturn(Optional.of(u));

        service.requestPasswordReset("  anne@example.com ");

        verify(tokenRepo).invalidateOpenTokens(eq(7), any());
        ArgumentCaptor<PasswordResetToken> saved = ArgumentCaptor.forClass(PasswordResetToken.class);
        verify(tokenRepo).save(saved.capture());
        ArgumentCaptor<String> link = ArgumentCaptor.forClass(String.class);
        verify(emailService).sendPasswordResetLink(eq("anne@example.com"), eq("midwife_anne"),
                link.capture(), eq(PasswordService.RESET_LINK_VALID_MINUTES));

        assertTrue(link.getValue().startsWith("https://moh.example.lk/reset-password?token="));
        String token = link.getValue().substring(link.getValue().indexOf("token=") + 6);
        assertEquals(hashService.hashSha256(token), saved.getValue().getTokenHash());
        assertNotEquals(token, saved.getValue().getTokenHash());
        assertTrue(saved.getValue().getExpiresAt().isAfter(LocalDateTime.now().plusMinutes(29)));
    }

    @Test
    void forgotPasswordForUnknownOrInactiveEmailDoesNothing() {
        when(userRepo.findByEmail("nobody@example.com")).thenReturn(Optional.empty());
        User inactive = user("OldPass123");
        inactive.setIsActive(false);
        when(userRepo.findByEmail("anne@example.com")).thenReturn(Optional.of(inactive));

        service.requestPasswordReset("nobody@example.com");
        service.requestPasswordReset("anne@example.com");

        verify(tokenRepo, never()).save(any());
        verifyNoInteractions(emailService);
    }

    // ---- reset password ----

    @Test
    void resetPasswordWithValidLinkChangesPasswordAndUsesLink() {
        User u = user("OldPass123");
        PasswordResetToken t = storedToken(u, "good-token", LocalDateTime.now().plusMinutes(10), null);

        service.resetPassword(reset("good-token", "NewPass456"));

        assertTrue(hashService.matches("NewPass456", u.getPasswordHash()));
        assertNotNull(t.getUsedAt());
        verify(tokenRepo).invalidateOpenTokens(eq(7), any());
    }

    @Test
    void resetPasswordRejectsUnknownExpiredOrUsedLinks() {
        User u = user("OldPass123");
        storedToken(u, "expired", LocalDateTime.now().minusMinutes(1), null);
        storedToken(u, "used", LocalDateTime.now().plusMinutes(10), LocalDateTime.now().minusMinutes(5));
        when(tokenRepo.findByTokenHash(hashService.hashSha256("unknown"))).thenReturn(Optional.empty());

        for (String token : new String[]{"expired", "used", "unknown"}) {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> service.resetPassword(reset(token, "NewPass456")));
            assertEquals(PasswordService.INVALID_LINK_MESSAGE, ex.getMessage());
        }
        assertTrue(hashService.matches("OldPass123", u.getPasswordHash()));
        verify(userRepo, never()).save(any());
    }

    @Test
    void resetPasswordRejectsWeakPasswordWithoutUsingLink() {
        User u = user("OldPass123");
        PasswordResetToken t = storedToken(u, "good-token", LocalDateTime.now().plusMinutes(10), null);

        assertThrows(IllegalArgumentException.class, () -> service.resetPassword(reset("good-token", "weak")));
        assertNull(t.getUsedAt());
        verify(userRepo, never()).save(any());
    }

    // ---- endpoint access ----

    @Test
    void forgotAndResetArePublicButChangePasswordNeedsToken() throws Exception {
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(
                new JwtService(Base64.getEncoder().encodeToString(new byte[32]), 60000));

        for (String path : new String[]{"/auth/forgot-password", "/auth/reset-password"}) {
            MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/moh-backend" + path);
            request.setContextPath("/api/v1/moh-backend");
            MockHttpServletResponse response = new MockHttpServletResponse();
            filter.doFilter(request, response, new MockFilterChain());
            assertEquals(200, response.getStatus(), path);
        }

        MockHttpServletRequest request = new MockHttpServletRequest("PUT", "/api/v1/moh-backend/auth/change-password");
        request.setContextPath("/api/v1/moh-backend");
        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(request, response, new MockFilterChain());
        assertEquals(401, response.getStatus());
    }
}
