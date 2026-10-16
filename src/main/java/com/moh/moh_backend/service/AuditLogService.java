package com.moh.moh_backend.service;

import com.moh.moh_backend.dto.AuditLogDtos;
import com.moh.moh_backend.model.AuditLog;
import com.moh.moh_backend.model.User;
import com.moh.moh_backend.repository.AuditLogRepository;
import com.moh.moh_backend.repository.UserRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class AuditLogService {
    static final int MAX_PAGE_SIZE = 100;

    private final AuditLogRepository repository;
    private final UserRepository userRepo;

    public AuditLogService(AuditLogRepository repository, UserRepository userRepo) {
        this.repository = repository;
        this.userRepo = userRepo;
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

    /** Filters for the Admin audit-log screen. Every field is optional. */
    public record SearchFilter(LocalDate from, LocalDate to, String username, String role,
                               String action, boolean failedOnly, String path) {}

    @Transactional(readOnly = true)
    public AuditLogDtos.PageResponse search(SearchFilter filter, int page, int size) {
        if (filter.from() != null && filter.to() != null && filter.from().isAfter(filter.to())) {
            throw new IllegalArgumentException("'From' date must be on or before 'To' date");
        }
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);

        // A username filter that matches nobody means no results
        Set<Integer> userIds = null;
        if (hasText(filter.username())) {
            userIds = userRepo.findByUsernameContainingIgnoreCase(filter.username().trim()).stream()
                    .map(User::getUserId)
                    .collect(Collectors.toSet());
            if (userIds.isEmpty()) {
                return toPage(Page.empty(PageRequest.of(safePage, safeSize)), Map.of());
            }
        }

        Page<AuditLog> result = repository.findAll(specification(filter, userIds),
                PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "createdAt", "auditId")));

        Set<Integer> pageUserIds = result.getContent().stream()
                .map(AuditLog::getUserId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());
        Map<Integer, String> usernames = userRepo.findAllById(pageUserIds).stream()
                .collect(Collectors.toMap(User::getUserId, User::getUsername));
        return toPage(result, usernames);
    }

    private static Specification<AuditLog> specification(SearchFilter filter, Set<Integer> userIds) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (filter.from() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), filter.from().atStartOfDay()));
            }
            if (filter.to() != null) {
                predicates.add(cb.lessThan(root.get("createdAt"), filter.to().plusDays(1).atStartOfDay()));
            }
            if (userIds != null) {
                predicates.add(root.get("userId").in(userIds));
            }
            if (hasText(filter.role())) {
                predicates.add(cb.equal(root.get("role"), filter.role().trim().toUpperCase()));
            }
            if (hasText(filter.action())) {
                predicates.add(cb.equal(root.get("action"), filter.action().trim().toUpperCase()));
            }
            if (filter.failedOnly()) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("responseStatus"), 400));
            }
            if (hasText(filter.path())) {
                predicates.add(cb.like(cb.lower(root.get("requestPath")),
                        "%" + filter.path().trim().toLowerCase() + "%"));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private static AuditLogDtos.PageResponse toPage(Page<AuditLog> result, Map<Integer, String> usernames) {
        AuditLogDtos.PageResponse resp = new AuditLogDtos.PageResponse();
        resp.content = result.getContent().stream().map(toEntry(usernames)).toList();
        resp.page = result.getNumber();
        resp.size = result.getSize();
        resp.totalElements = result.getTotalElements();
        resp.totalPages = result.getTotalPages();
        return resp;
    }

    private static Function<AuditLog, AuditLogDtos.Entry> toEntry(Map<Integer, String> usernames) {
        return log -> {
            AuditLogDtos.Entry e = new AuditLogDtos.Entry();
            e.auditId = log.getAuditId();
            e.userId = log.getUserId();
            e.username = log.getUserId() != null ? usernames.get(log.getUserId()) : null;
            e.role = log.getRole();
            e.httpMethod = log.getHttpMethod();
            e.requestPath = log.getRequestPath();
            e.action = log.getAction();
            e.responseStatus = log.getResponseStatus();
            e.clientIp = log.getClientIp();
            e.createdAt = log.getCreatedAt() != null ? log.getCreatedAt().toString() : null;
            return e;
        };
    }

    private static boolean hasText(String s) {
        return s != null && !s.isBlank();
    }
}
