package com.moh.moh_backend.model;

import com.moh.moh_backend.Enum.VisitRequestStatus;
import com.moh.moh_backend.Enum.VisitRequestType;
import com.moh.moh_backend.Enum.VisitRequestUrgency;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** A mother's request for a special home visit or clinic consultation (table created by DB/08_visit_requests.sql). */
@Entity
@Table(name = "VISIT_REQUEST")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VisitRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "request_id")
    private Integer requestId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mother_id", nullable = false)
    private Mother mother;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "phm_area_id", nullable = false)
    private PhmArea phmArea;

    @Column(name = "request_type", nullable = false, length = 30)
    @Enumerated(EnumType.STRING)
    private VisitRequestType requestType;

    @Column(name = "urgency", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private VisitRequestUrgency urgency = VisitRequestUrgency.ROUTINE;

    @Column(name = "reason", nullable = false, columnDefinition = "TEXT")
    private String reason;

    @Column(name = "preferred_date")
    private LocalDate preferredDate;

    @Column(name = "contact_number", length = 20)
    private String contactNumber;

    @Column(name = "status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private VisitRequestStatus status = VisitRequestStatus.PENDING;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "handled_by_midwife_id")
    private Midwife handledBy;

    @Column(name = "scheduled_at")
    private LocalDateTime scheduledAt;

    @Column(name = "midwife_response", columnDefinition = "TEXT")
    private String midwifeResponse;

    @Column(name = "acknowledged_at")
    private LocalDateTime acknowledgedAt;

    @Column(name = "closed_at")
    private LocalDateTime closedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
