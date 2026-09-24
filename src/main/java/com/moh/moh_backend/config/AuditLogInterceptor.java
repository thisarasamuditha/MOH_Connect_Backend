package com.moh.moh_backend.config;

import com.moh.moh_backend.service.AuditLogService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuditLogInterceptor implements HandlerInterceptor {
    private final AuditLogService auditLogService;

    public AuditLogInterceptor(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception exception) {
        if (!request.getRequestURI().startsWith(request.getContextPath() + "/api/")
                || !isMutation(request.getMethod())
                || "OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return;
        }

        String action = switch (request.getMethod().toUpperCase()) {
            case "POST" -> "CREATE";
            case "DELETE" -> "DELETE";
            default -> "UPDATE";
        };
        try {
            auditLogService.record(
                    (Integer) request.getAttribute(JwtAuthenticationFilter.USER_ID_ATTRIBUTE),
                    (String) request.getAttribute(JwtAuthenticationFilter.ROLE_ATTRIBUTE),
                    request.getMethod(), request.getRequestURI(), action,
                    response.getStatus(), request.getRemoteAddr());
        } catch (RuntimeException ignored) {
            // Audit failures must not change the already-completed clinical response.
        }
    }

    private boolean isMutation(String method) {
        return "POST".equalsIgnoreCase(method)
                || "PUT".equalsIgnoreCase(method)
                || "PATCH".equalsIgnoreCase(method)
                || "DELETE".equalsIgnoreCase(method);
    }
}
