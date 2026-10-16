package com.moh.moh_backend.dto;

import java.util.List;

/**
 * DTOs for the Admin audit-log screen.
 */
public class AuditLogDtos {

    public static class Entry {
        public Long auditId;
        public Integer userId;
        public String username;   // null for anonymous calls (login, forgot password)
        public String role;
        public String httpMethod;
        public String requestPath;
        public String action;
        public Integer responseStatus;
        public String clientIp;
        public String createdAt;
    }

    public static class PageResponse {
        public List<Entry> content;
        public int page;
        public int size;
        public long totalElements;
        public int totalPages;
    }
}
