package com.moh.moh_backend.service;

import com.moh.moh_backend.Enum.UserRole;
import com.moh.moh_backend.config.AdminBootstrap;
import com.moh.moh_backend.config.JwtAuthenticationFilter;
import com.moh.moh_backend.config.RequireRoles;
import com.moh.moh_backend.controller.AuthController;
import com.moh.moh_backend.dto.AuthDtos;
import com.moh.moh_backend.model.User;
import com.moh.moh_backend.repository.*;
import com.moh.moh_backend.util.JwtService;
import com.moh.moh_backend.util.PasswordHashService;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.Base64;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * /auth/register is admin-only and creates staff accounts only.
 */
class StaffRegistrationTests {

    private final UserRepository userRepo = mock(UserRepository.class);
    private final AuthService authService = new AuthService(userRepo, new PasswordHashService(),
            jwtService(), mock(DoctorRepository.class), mock(MidwifeRepository.class),
            mock(MotherRepository.class), mock(PhmAreaRepository.class));

    private static JwtService jwtService() {
        return new JwtService(Base64.getEncoder().encodeToString(new byte[32]), 60000);
    }

    private static AuthDtos.RegisterRequest request(String role) {
        AuthDtos.RegisterRequest req = new AuthDtos.RegisterRequest();
        req.username = "new_admin";
        req.email = "new.admin@example.com";
        req.password = "Secret@123";
        req.role = role;
        return req;
    }

    @Test
    void registerEndpointRequiresAdminRole() throws Exception {
        RequireRoles roles = AuthController.class
                .getMethod("register", AuthDtos.RegisterRequest.class)
                .getAnnotation(RequireRoles.class);
        assertNotNull(roles);
        assertArrayEquals(new String[]{"ADMIN"}, roles.value());
    }

    @Test
    void registerWithoutTokenIsRejectedByFilter() throws Exception {
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtService());
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/moh-backend/auth/register");
        request.setContextPath("/api/v1/moh-backend");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertEquals(401, response.getStatus());
        assertNull(chain.getRequest(), "request must not reach the controller");
    }

    @Test
    void registerRejectsMotherRole() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> authService.register(request("MOTHER")));
        assertTrue(e.getMessage().contains("midwife"));
        verify(userRepo, never()).save(any());
    }

    @Test
    void registerRejectsUnknownRole() {
        assertThrows(IllegalArgumentException.class, () -> authService.register(request("SUPERUSER")));
        assertThrows(IllegalArgumentException.class, () -> authService.register(request(null)));
        verify(userRepo, never()).save(any());
    }

    @Test
    void registerCreatesStaffWithoutIssuingToken() {
        when(userRepo.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setUserId(42);
            return u;
        });

        AuthDtos.AuthResponse resp = authService.register(request("admin"));

        assertEquals("ADMIN", resp.role);
        assertEquals(42, resp.userId);
        assertNull(resp.token, "the admin's session must not be replaced by the new user's");
    }

    @Test
    void bootstrapDoesNothingWhenAnActiveAdminExists() {
        User existing = new User();
        existing.setRole(UserRole.ADMIN);
        existing.setIsActive(true);
        when(userRepo.findByRole(UserRole.ADMIN)).thenReturn(List.of(existing));

        new AdminBootstrap(userRepo, new PasswordHashService(), "boot", "boot@example.com", "Secret@123").run();

        verify(userRepo, never()).save(any());
    }

    @Test
    void bootstrapDoesNothingWithoutVariables() {
        new AdminBootstrap(userRepo, new PasswordHashService(), "", "", "").run();
        verifyNoInteractions(userRepo);
    }

    @Test
    void bootstrapCreatesFirstAdmin() {
        when(userRepo.findByRole(UserRole.ADMIN)).thenReturn(List.of());

        new AdminBootstrap(userRepo, new PasswordHashService(), "boot", "boot@example.com", "Secret@123").run();

        verify(userRepo).save(argThat(u -> u.getRole() == UserRole.ADMIN
                && "boot".equals(u.getUsername())
                && u.getPasswordHash().startsWith("$2")));
    }
}
