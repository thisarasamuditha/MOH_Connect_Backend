package com.moh.moh_backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
@Data
@Entity
@Table(name = "DOCTOR")
@AllArgsConstructor
@SuperBuilder
@NoArgsConstructor
public class Doctor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "doctor_id")
    private Integer doctorId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true, nullable = false)
    private User user;

    @OneToOne (fetch = FetchType.LAZY)
    @JoinColumn(name = "phm_area_id", nullable = false)
    private PhmArea phmArea;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "specialization", length = 255)
    private String specialization;

    @Column(name = "contact_number", length = 20)
    private String contactNumber;

    @Column(name = "email", length = 255)
    private String email;

    @Column(name = "license_number", unique = true, nullable = false, length = 100)
    private String licenseNumber;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
