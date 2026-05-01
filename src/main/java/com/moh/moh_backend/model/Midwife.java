package com.moh.moh_backend.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "MIDWIFE")
public class Midwife {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "midwife_id")
    private Integer midwifeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true, nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "phm_area_id", nullable = false)
    private PhmArea phmArea;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "contact_number", length = 20)
    private String contactNumber;

    @Column(name = "email", length = 255)
    private String email;

    @Column(name = "assignment_date")
    private LocalDate assignmentDate;

    @Column(name = "qualifications", length = 500)
    private String qualifications;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // Constructors
    public Midwife() {
    }

    public Midwife(User user, PhmArea phmArea, String name, String contactNumber, String email) {
        this.user = user;
        this.phmArea = phmArea;
        this.name = name;
        this.contactNumber = contactNumber;
        this.email = email;
    }

    // Getters and Setters
    public Integer getMidwifeId() {
        return midwifeId;
    }

    public void setMidwifeId(Integer midwifeId) {
        this.midwifeId = midwifeId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public PhmArea getPhmArea() {
        return phmArea;
    }

    public void setPhmArea(PhmArea phmArea) {
        this.phmArea = phmArea;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public LocalDate getAssignmentDate() {
        return assignmentDate;
    }

    public void setAssignmentDate(LocalDate assignmentDate) {
        this.assignmentDate = assignmentDate;
    }

    public String getQualifications() {
        return qualifications;
    }

    public void setQualifications(String qualifications) {
        this.qualifications = qualifications;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
