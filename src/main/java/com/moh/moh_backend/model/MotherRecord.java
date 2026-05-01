package com.moh.moh_backend.model;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "MOTHER_RECORD")
public class MotherRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "record_id")
    private Integer recordId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pregnancy_id", nullable = false)
    private Pregnancy pregnancy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "midwife_id")
    private Midwife midwife;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id")
    private Doctor doctor;

    @Column(name = "record_date", nullable = false)
    private LocalDate recordDate;

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

    // Constructors
    public MotherRecord() {}

    // Getters and Setters
    public Integer getRecordId() {
        return recordId;
    }

    public void setRecordId(Integer recordId) {
        this.recordId = recordId;
    }

    public Pregnancy getPregnancy() {
        return pregnancy;
    }

    public void setPregnancy(Pregnancy pregnancy) {
        this.pregnancy = pregnancy;
    }

    public Midwife getMidwife() {
        return midwife;
    }

    public void setMidwife(Midwife midwife) {
        this.midwife = midwife;
    }

    public Doctor getDoctor() {
        return doctor;
    }

    public void setDoctor(Doctor doctor) {
        this.doctor = doctor;
    }

    public LocalDate getRecordDate() {
        return recordDate;
    }

    public void setRecordDate(LocalDate recordDate) {
        this.recordDate = recordDate;
    }

    public Integer getGestationalAge() {
        return gestationalAge;
    }

    public void setGestationalAge(Integer gestationalAge) {
        this.gestationalAge = gestationalAge;
    }

    public Float getWeight() {
        return weight;
    }

    public void setWeight(Float weight) {
        this.weight = weight;
    }

    public Float getBmi() {
        return bmi;
    }

    public void setBmi(Float bmi) {
        this.bmi = bmi;
    }

    public String getBloodPressure() {
        return bloodPressure;
    }

    public void setBloodPressure(String bloodPressure) {
        this.bloodPressure = bloodPressure;
    }

    public Float getShf() {
        return shf;
    }

    public void setShf(Float shf) {
        this.shf = shf;
    }

    public String getFindings() {
        return findings;
    }

    public void setFindings(String findings) {
        this.findings = findings;
    }

    public String getRecommendations() {
        return recommendations;
    }

    public void setRecommendations(String recommendations) {
        this.recommendations = recommendations;
    }

    public String getComplications() {
        return complications;
    }

    public void setComplications(String complications) {
        this.complications = complications;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public LocalDate getNextVisitDate() {
        return nextVisitDate;
    }

    public void setNextVisitDate(LocalDate nextVisitDate) {
        this.nextVisitDate = nextVisitDate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
