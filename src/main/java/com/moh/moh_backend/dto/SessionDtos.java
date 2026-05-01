package com.moh.moh_backend.dto;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

public class SessionDtos {

    // ── Session Type ──────────────────────────────────────

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class SessionTypeRequest {
        private String typeName;
        private String description;
        private String targetAudience; // PREGNANT_MOTHERS, NEW_MOTHERS, FAMILIES, ALL
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class SessionTypeResponse {
        private Integer sessionTypeId;
        private String typeName;
        private String description;
        private String targetAudience;
    }

    // ── Clinic Session ────────────────────────────────────

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class SessionCreateRequest {
        private Integer midwifeId;
        private Integer sessionTypeId;
        private Integer phmAreaId;
        private LocalDate sessionDate;
        private LocalTime startTime;
        private LocalTime endTime;
        private String topic;
        private String venue;
        private String description;
        private Integer capacity;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class SessionUpdateRequest {
        private LocalDate sessionDate;
        private LocalTime startTime;
        private LocalTime endTime;
        private String topic;
        private String venue;
        private String description;
        private Integer capacity;
        private String status; // SCHEDULED, COMPLETED, CANCELLED
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class SessionResponse {
        private Integer sessionId;
        private Integer midwifeId;
        private String midwifeName;
        private Integer sessionTypeId;
        private String sessionTypeName;
        private Integer phmAreaId;
        private String phmAreaName;
        private LocalDate sessionDate;
        private LocalTime startTime;
        private LocalTime endTime;
        private String topic;
        private String venue;
        private String description;
        private Integer capacity;
        private String status;
        private String createdAt;
    }

    // ── Attendance ────────────────────────────────────────

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class AttendanceCreateRequest {
        private Integer sessionId;
        private Integer motherId;
        private String notes;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class AttendanceMarkRequest {
        private Boolean attended;
        private String feedback;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class AttendanceResponse {
        private Integer attendanceId;
        private Integer sessionId;
        private String sessionTopic;
        private LocalDate sessionDate;
        private Integer motherId;
        private String motherName;
        private Boolean attended;
        private String attendanceTime;
        private String notes;
        private String feedback;
        private String createdAt;
    }
}
