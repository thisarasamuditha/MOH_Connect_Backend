package com.moh.moh_backend.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "PREGNANCY", indexes = {
    @Index(name = "idx_mother_status", columnList = "mother_id, pregnancy_status")
})
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

    public enum DeliveryType {
        NORMAL, C_SECTION, ASSISTED, OTHER
    }

    public enum PregnancyStatus {
        ACTIVE, COMPLETED, TERMINATED, MISCARRIAGE
    }

    public enum RiskLevel {
        LOW, MEDIUM, HIGH
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Constructors
    public Pregnancy() {}

    // Getters and Setters
    public Integer getPregnancyId() {
        return pregnancyId;
    }

    public void setPregnancyId(Integer pregnancyId) {
        this.pregnancyId = pregnancyId;
    }

    public Mother getMother() {
        return mother;
    }

    public void setMother(Mother mother) {
        this.mother = mother;
    }

    public String getPregnancyNumber() {
        return pregnancyNumber;
    }

    public void setPregnancyNumber(String pregnancyNumber) {
        this.pregnancyNumber = pregnancyNumber;
    }

    public LocalDate getLmpDate() {
        return lmpDate;
    }

    public void setLmpDate(LocalDate lmpDate) {
        this.lmpDate = lmpDate;
    }

    public LocalDate getEddDate() {
        return eddDate;
    }

    public void setEddDate(LocalDate eddDate) {
        this.eddDate = eddDate;
    }

    public LocalDate getDeliveryDate() {
        return deliveryDate;
    }

    public void setDeliveryDate(LocalDate deliveryDate) {
        this.deliveryDate = deliveryDate;
    }

    public DeliveryType getDeliveryType() {
        return deliveryType;
    }

    public void setDeliveryType(DeliveryType deliveryType) {
        this.deliveryType = deliveryType;
    }

    public PregnancyStatus getPregnancyStatus() {
        return pregnancyStatus;
    }

    public void setPregnancyStatus(PregnancyStatus pregnancyStatus) {
        this.pregnancyStatus = pregnancyStatus;
    }

    public Integer getGravida() {
        return gravida;
    }

    public void setGravida(Integer gravida) {
        this.gravida = gravida;
    }

    public Integer getPara() {
        return para;
    }

    public void setPara(Integer para) {
        this.para = para;
    }

    public RiskLevel getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(RiskLevel riskLevel) {
        this.riskLevel = riskLevel;
    }

    public String getRiskFactors() {
        return riskFactors;
    }

    public void setRiskFactors(String riskFactors) {
        this.riskFactors = riskFactors;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
