package com.moh.moh_backend.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import com.moh.moh_backend.Enum.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "PREGNANCY", indexes = {
    @Index(name = "idx_mother_status", columnList = "mother_id, pregnancy_status")
})
@SuperBuilder
@NoArgsConstructor
@Data
@AllArgsConstructor
public class Pregnancy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pregnancy_id")
    private Integer pregnancyId;

    @ManyToOne
    @JoinColumn(name = "mother_id", nullable = false)
    private Mother mother;

    @Column(name = "pregnancy_number", length = 50, unique = true, nullable = false)
    private String pregnancyNumber;

    @Column(name = "lmp_date", nullable = false)
    private LocalDate lmpDate;

    @Column(name = "edd_date", nullable = false)
    private LocalDate eddDate;

    @Column(name = "delivery_date")
    private LocalDate deliveryDate;

    @Column(name = "delivery_type")
    @Enumerated(EnumType.STRING)
    private DeliveryType deliveryType;

    @Column(name = "pregnancy_status")
    @Enumerated(EnumType.STRING)
    private PregnancyStatus pregnancyStatus = PregnancyStatus.ACTIVE;

    @Column(name = "gravida")
    private Integer gravida = 1;

    @Column(name = "para")
    private Integer para = 0;

    @Column(name = "risk_level")
    @Enumerated(EnumType.STRING)
    private RiskLevel riskLevel = RiskLevel.LOW;

    @Column(name = "risk_factors", columnDefinition = "TEXT")
    private String riskFactors;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;


    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

}
