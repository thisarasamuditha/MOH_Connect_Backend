package com.moh.moh_backend.model;

import com.moh.moh_backend.Enum.TargetGroup;
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

    /** Outdated schedule rows are kept (past vaccinations reference them) but marked inactive. */
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    /** Given only when a condition applies (e.g. BCG 2nd dose if no scar); never reported as overdue. */
    @Column(name = "is_conditional", nullable = false)
    private Boolean isConditional = false;

    /** null = all children; FEMALE/MALE = only that sex (e.g. HPV for girls). */
    @Column(name = "target_gender", length = 10)
    private String targetGender;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (isActive == null) isActive = true;
        if (isConditional == null) isConditional = false;
    }

    /** True unless explicitly deactivated (null counts as active). */
    @com.fasterxml.jackson.annotation.JsonIgnore
    public boolean isCurrent() {
        return !Boolean.FALSE.equals(isActive);
    }
}
