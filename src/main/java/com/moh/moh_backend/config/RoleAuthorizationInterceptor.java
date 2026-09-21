package com.moh.moh_backend.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Arrays;

@Component
public class RoleAuthorizationInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod handlerMethod)) return true;

        RequireRoles required = AnnotationUtils.findAnnotation(handlerMethod.getMethod(), RequireRoles.class);
        if (required == null) {
            required = AnnotationUtils.findAnnotation(handlerMethod.getBeanType(), RequireRoles.class);
        }
        if (required == null) return true;

        String role = (String) request.getAttribute(JwtAuthenticationFilter.ROLE_ATTRIBUTE);
        boolean allowed = role != null && Arrays.stream(required.value())
                .anyMatch(expected -> expected.equalsIgnoreCase(role));
        if (!allowed) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Insufficient role\"}");
            return false;
        }
        return true;
    }
}