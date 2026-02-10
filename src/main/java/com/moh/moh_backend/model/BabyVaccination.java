package com.moh.moh_backend.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "BABY_VACCINATION")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BabyVaccination {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "vaccination_id")
    private Integer vaccinationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "baby_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Baby baby;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "midwife_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Midwife midwife;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "schedule_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private VaccineSchedule schedule;

    @Column(name = "vaccination_date", nullable = false)
    private LocalDate vaccinationDate;

    @Column(name = "batch_number", length = 100)
    private String batchNumber;

    @Column(name = "manufacturer", length = 255)
    private String manufacturer;

    @Column(name = "next_dose_date")
    private LocalDate nextDoseDate;

    @Column(name = "adverse_reaction", columnDefinition = "TEXT")
    private String adverseReaction;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
