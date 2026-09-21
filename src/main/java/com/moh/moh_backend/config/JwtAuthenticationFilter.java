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
        if (!request.getRequestURI().startsWith(request.getContextPath() + "/api/")) {
            filterChain.doFilter(request, response);
            return;
        }

        String path = request.getRequestURI().substring(request.getContextPath().length());
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())
                || "/api/auth/login".equals(path)
                || "/api/auth/register".equals(path)
                || "/api/hello".equals(path)) {
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