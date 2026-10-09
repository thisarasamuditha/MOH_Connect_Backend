package com.moh.moh_backend.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "MOTHER_RECORD")
@AllArgsConstructor
@NoArgsConstructor
@Data
@SuperBuilder
public class MotherRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "record_id")
    private Integer recordId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pregnancy_id", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Pregnancy pregnancy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "midwife_id")
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "user", "phmArea"})
    private Midwife midwife;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id")
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "user", "phmArea"})
    private Doctor doctor;

    @com.fasterxml.jackson.annotation.JsonProperty("pregnancyId")
    public Integer getPregnancyId() {
        return pregnancy != null ? pregnancy.getPregnancyId() : null;
    }

    @Column(name = "record_date", nullable = false)
    private LocalDate recordDate;

    @Column(name = "visit_type", length = 10)
    private String visitType;

    @Column(name = "verification_status", nullable = false, length = 20)
    private String verificationStatus = "SUBMITTED";

    @Column(name = "review_comment", columnDefinition = "TEXT")
    private String reviewComment;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @Column(name = "gestational_age")
    private Integer gestationalAge;

    @Column(name = "weight")
    private Float weight;

    @Column(name = "bmi")
    private Float bmi;

    @Column(name = "blood_pressure", length = 50)
    private String bloodPressure;

    @Column(name = "shf")
    private Float shf;

    @Column(name = "findings", columnDefinition = "TEXT")
    private String findings;

    @Column(name = "recommendations", columnDefinition = "TEXT")
    private String recommendations;

    @Column(name = "complications", columnDefinition = "TEXT")
    private String complications;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "next_visit_date")
    private LocalDate nextVisitDate;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

}
