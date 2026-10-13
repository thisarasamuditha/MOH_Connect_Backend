package com.moh.moh_backend.dto;

import com.moh.moh_backend.model.VisitRequest;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class VisitRequestDtos {

    /** Sent by a mother. */
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class CreateRequest {
        private String requestType;   // HOME_VISIT, CLINIC_CONSULTATION
        private String urgency;       // ROUTINE, URGENT
        private String reason;
        private LocalDate preferredDate;
        private String contactNumber;
    }

    /** Sent by a midwife to move a request along. */
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class RespondRequest {
        private String status;        // ACKNOWLEDGED, SCHEDULED, COMPLETED, DECLINED
        private LocalDateTime scheduledAt;
        private String response;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class Response {
        private Integer requestId;
        private Integer motherId;
        private String motherName;
        private String motherNic;
        private String motherAddress;
        private String requestType;
        private String urgency;
        private String reason;
        private LocalDate preferredDate;
        private String contactNumber;
        private String status;
        private String handledByName;
        private LocalDateTime scheduledAt;
        private String midwifeResponse;
        private LocalDateTime acknowledgedAt;
        private LocalDateTime closedAt;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public static Response from(VisitRequest r) {
            return Response.builder()
                    .requestId(r.getRequestId())
                    .motherId(r.getMother().getMotherId())
                    .motherName(r.getMother().getName())
                    .motherNic(r.getMother().getNic())
                    .motherAddress(r.getMother().getAddress())
                    .requestType(r.getRequestType().name())
                    .urgency(r.getUrgency().name())
                    .reason(r.getReason())
                    .preferredDate(r.getPreferredDate())
                    .contactNumber(r.getContactNumber())
                    .status(r.getStatus().name())
                    .handledByName(r.getHandledBy() != null ? r.getHandledBy().getName() : null)
                    .scheduledAt(r.getScheduledAt())
                    .midwifeResponse(r.getMidwifeResponse())
                    .acknowledgedAt(r.getAcknowledgedAt())
                    .closedAt(r.getClosedAt())
                    .createdAt(r.getCreatedAt())
                    .updatedAt(r.getUpdatedAt())
                    .build();
        }
    }

    /** Midwife menu badge. */
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class OpenCount {
        private long open;
        private long urgent;
    }

    /** The PHM(s) a mother can call directly. */
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class MidwifeContact {
        private String name;
        private String contactNumber;
        private String phmAreaName;
    }
}
