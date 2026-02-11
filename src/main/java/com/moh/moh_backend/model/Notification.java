package com.moh.moh_backend.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "NOTIFICATION", indexes = {
    @Index(name = "idx_mother_sent", columnList = "mother_id, sent_date")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_id")
    private Integer notificationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "type_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private NotificationType notificationType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "midwife_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "user", "phmArea"})
    private Midwife midwife;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mother_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "user", "phmArea"})
    private Mother mother;

    @Column(name = "sent_date")
    private LocalDateTime sentDate;

    @Column(name = "message", columnDefinition = "TEXT", nullable = false)
    private String message;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private NotificationStatus status = NotificationStatus.PENDING;

    @Column(name = "delivery_method")
    @Enumerated(EnumType.STRING)
    private DeliveryMethod deliveryMethod;

    @Column(name = "event_date")
    private LocalDate eventDate;

    @Column(name = "event_type", length = 255)
    private String eventType;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    @Column(name = "responded_at")
    private LocalDateTime respondedAt;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public enum NotificationStatus {
        PENDING, SENT, DELIVERED, FAILED
    }

    public enum DeliveryMethod {
        SMS, EMAIL, WHATSAPP, CALL
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (sentDate == null) {
            sentDate = LocalDateTime.now();
        }
    }
}
