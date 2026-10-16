package com.moh.moh_backend.controller;

import com.moh.moh_backend.config.RequireRoles;
import com.moh.moh_backend.dto.AuditLogDtos;
import com.moh.moh_backend.service.AuditLogService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * Admin audit-log screen: every create/update/delete call, newest first.
 * GET /audit-logs?from=2026-10-01&to=2026-10-16&username=&role=&action=&failedOnly=&path=&page=0&size=25
 */
@RestController
@RequestMapping("/audit-logs")
@RequireRoles("ADMIN")
public class AuditLogController {
    private final AuditLogService auditLogService;

    public AuditLogController(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @GetMapping
    public ResponseEntity<AuditLogDtos.PageResponse> search(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String action,
            @RequestParam(defaultValue = "false") boolean failedOnly,
            @RequestParam(required = false) String path,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int size) {
        return ResponseEntity.ok(auditLogService.search(
                new AuditLogService.SearchFilter(from, to, username, role, action, failedOnly, path),
                page, size));
    }
}
