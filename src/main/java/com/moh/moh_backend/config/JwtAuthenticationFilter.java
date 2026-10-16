package com.moh.moh_backend.config;

import com.moh.moh_backend.util.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    public static final String USER_ID_ATTRIBUTE = "moh.userId";
    public static final String ROLE_ATTRIBUTE = "moh.role";

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        // All endpoints live directly under the context path (/api/v1/moh-backend), e.g. /mothers/...
        String path = request.getRequestURI().substring(request.getContextPath().length());
        boolean isPublicGet = "GET".equalsIgnoreCase(request.getMethod()) && (
                path.startsWith("/phm-areas")
                || path.startsWith("/session-types")
                || path.startsWith("/section-types")
                || path.startsWith("/vaccine-schedules")
        );

        if ("OPTIONS".equalsIgnoreCase(request.getMethod())
                || "/auth/login".equals(path)
                || "/auth/forgot-password".equals(path)
                || "/auth/reset-password".equals(path)
                || "/".equals(path)
                || path.isEmpty()
                || isPublicGet) {
            String authorization = request.getHeader("Authorization");
            if (authorization != null && authorization.startsWith("Bearer ")) {
                String token = authorization.substring("Bearer ".length()).trim();
                if (jwtService.isValid(token)) {
                    request.setAttribute(USER_ID_ATTRIBUTE, jwtService.getUserId(token));
                    request.setAttribute(ROLE_ATTRIBUTE, jwtService.getRole(token));
                }
            }
            filterChain.doFilter(request, response);
            return;
        }

        String authorization = request.getHeader("Authorization");
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            unauthorized(response, "Missing Bearer token");
            return;
        }

        String token = authorization.substring("Bearer ".length()).trim();
        if (!jwtService.isValid(token)) {
            unauthorized(response, "Invalid or expired token");
            return;
        }

        Integer userId = jwtService.getUserId(token);
        String role = jwtService.getRole(token);
        if (userId == null || role == null || role.isBlank()) {
            unauthorized(response, "Token is missing required claims");
            return;
        }

        request.setAttribute(USER_ID_ATTRIBUTE, userId);
        request.setAttribute(ROLE_ATTRIBUTE, role);
        filterChain.doFilter(request, response);
    }

    private void unauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.getWriter().write("{\"error\":\"" + message + "\"}");
    }
}