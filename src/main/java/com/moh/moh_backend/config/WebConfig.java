package com.moh.moh_backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * CORS configuration to allow frontend access from React app
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {
    private final RoleAuthorizationInterceptor roleAuthorizationInterceptor;
    private final AuditLogInterceptor auditLogInterceptor;

    public WebConfig(RoleAuthorizationInterceptor roleAuthorizationInterceptor,
                     AuditLogInterceptor auditLogInterceptor) {
        this.roleAuthorizationInterceptor = roleAuthorizationInterceptor;
        this.auditLogInterceptor = auditLogInterceptor;
    }

    @Override
    public void addInterceptors(org.springframework.web.servlet.config.annotation.InterceptorRegistry registry) {
        // Patterns are relative to the context path (/api/v1/moh-backend)
        registry.addInterceptor(roleAuthorizationInterceptor).addPathPatterns("/**").excludePathPatterns("/error");
        registry.addInterceptor(auditLogInterceptor).addPathPatterns("/**").excludePathPatterns("/error");
    }
    
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:3000", "http://localhost:3002") // React ports
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
