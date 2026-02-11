package com.moh.moh_backend.dto;

import lombok.*;

import java.time.LocalDate;

public class NotificationDtos {

    // ── Notification Type ─────────────────────────────────

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class NotificationTypeRequest {
        private String typeName;
        private String description;
        private String template;
        private String priority; // LOW, MEDIUM, HIGH, URGENT
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class NotificationTypeResponse {
        private Integer typeId;
        private String typeName;
        private String description;
        private String template;
        private String priority;
    }

    // ── Notification ──────────────────────────────────────

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class NotificationCreateRequest {
        private Integer typeId;
        private Integer midwifeId;
        private Integer motherId;
        private String message;
        private String deliveryMethod; // SMS, EMAIL, WHATSAPP, CALL
        private LocalDate eventDate;
        private String eventType;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class NotificationResponse {
        private Integer notificationId;
        private Integer typeId;
        private String typeName;
        private String priority;
        private Integer midwifeId;
        private String midwifeName;
        private Integer motherId;
        private String motherName;
        private String message;
        private String status;
        private String deliveryMethod;
        private String sentDate;
        private LocalDate eventDate;
        private String eventType;
        private String readAt;
        private String respondedAt;
        private String createdAt;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class NotificationStatusUpdate {
        private String status; // PENDING, SENT, DELIVERED, FAILED
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class UnreadCountResponse {
        private Integer motherId;
        private long unreadCount;
    }
}
