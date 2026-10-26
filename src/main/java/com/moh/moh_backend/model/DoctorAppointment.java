package com.moh.moh_backend.model;

import com.moh.moh_backend.Enum.AppointmentPriority;
import com.moh.moh_backend.Enum.DoctorAppointmentStatus;
import com.moh.moh_backend.Enum.PatientType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "DOCTOR_APPOINTMENT", indexes = {
    @Index(name = "idx_doc_app_doctor", columnList = "doctor_id, status"),
    @Index(name = "idx_doc_app_patient", columnList = "patient_type, mother_id, baby_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DoctorAppointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "appointment_id")
    private Integer appointmentId;

    @Enumerated(EnumType.STRING)
    @Column(name = "patient_type", nullable = false, length = 20)
    private PatientType patientType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mother_id")
    private Mother mother;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pregnancy_id")
    private Pregnancy pregnancy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "baby_id")
    private Baby baby;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "referred_by_user_id")
    private User referredBy;

    @Column(name = "reason", nullable = false, columnDefinition = "TEXT")
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false, length = 20)
    @Builder.Default
    private AppointmentPriority priority = AppointmentPriority.HIGH_RISK;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private DoctorAppointmentStatus status = DoctorAppointmentStatus.DRAFT;

    @Column(name = "appointment_date")
    private LocalDate appointmentDate;

    @Column(name = "appointment_time", length = 30)
    private String appointmentTime;

    @Column(name = "scheduled_at")
    private LocalDateTime scheduledAt;

    @Column(name = "clinical_notes", columnDefinition = "TEXT")
    private String clinicalNotes;

    @Column(name = "diagnosis", columnDefinition = "TEXT")
    private String diagnosis;

    @Column(name = "prescriptions", columnDefinition = "TEXT")
    private String prescriptions;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = DoctorAppointmentStatus.DRAFT;
        }
        if (priority == null) {
            priority = AppointmentPriority.HIGH_RISK;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
