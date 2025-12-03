package com.moh.moh_backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "PHM_AREA")
public class PhmArea {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "phm_area_id")
    private Integer phmAreaId;

    @Column(name = "area_name", nullable = false, length = 255)
    private String areaName;

    @Column(name = "area_code", unique = true, nullable = false, length = 50)
    private String areaCode;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // Constructors
    public PhmArea() {
    }

    public PhmArea(String areaName, String areaCode, String description) {
        this.areaName = areaName;
        this.areaCode = areaCode;
        this.description = description;
    }

    // Getters and Setters
    public Integer getPhmAreaId() {
        return phmAreaId;
    }

    public void setPhmAreaId(Integer phmAreaId) {
        this.phmAreaId = phmAreaId;
    }

    public String getAreaName() {
        return areaName;
    }

    public void setAreaName(String areaName) {
        this.areaName = areaName;
    }

    public String getAreaCode() {
        return areaCode;
    }

    public void setAreaCode(String areaCode) {
        this.areaCode = areaCode;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
