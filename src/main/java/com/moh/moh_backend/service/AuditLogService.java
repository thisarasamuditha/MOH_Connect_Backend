package com.moh.moh_backend.service;

import com.moh.moh_backend.model.AuditLog;
import com.moh.moh_backend.repository.AuditLogRepository;
import org.springframework.stereotype.Service;

@Service
public class AuditLogService {
    private final AuditLogRepository repository;

    public AuditLogService(AuditLogRepository repository) {
        this.repository = repository;
    }

    public void record(Integer userId, String role, String method, String path,
                       String action, int status, String clientIp) {
        AuditLog audit = new AuditLog();
        audit.setUserId(userId);
        audit.setRole(role);
        audit.setHttpMethod(method);
        audit.setRequestPath(path);
        audit.setAction(action);
        audit.setResponseStatus(status);
        audit.setClientIp(clientIp);
        repository.save(audit);
    }
}
