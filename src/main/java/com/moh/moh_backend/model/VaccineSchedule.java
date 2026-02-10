package com.moh.moh_backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "VACCINE_SCHEDULE")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VaccineSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "schedule_id")
    private Integer scheduleId;

    @Column(name = "vaccine_name", nullable = false, length = 255)
    private String vaccineName;

    @Column(name = "target_group", nullable = false)
    @Enumerated(EnumType.STRING)
    private TargetGroup targetGroup;

    @Column(name = "dose_number", nullable = false)
    private Integer doseNumber;

    @Column(name = "recommended_age_days")
    private Integer recommendedAgeDays;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
