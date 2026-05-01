package com.moh.moh_backend.model;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "BABY")

public class Baby {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "baby_id")
    private Integer babyId;

    @Column(name = "pregnancy_id", nullable = false)
    private Integer pregnancyId;

    @Column(name = "mother_id", nullable = false)
    private Integer motherId;

    @Column(name = "name", length = 255)
    private String name;

    @Column(name = "date_of_birth", nullable = false)
    private LocalDate dateOfBirth;

    @Column(name = "gender", nullable = false)
    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Column(name = "birth_weight")
    private Float birthWeight;

    @Column(name = "birth_height")
    private Float birthHeight;

    @Column(name = "birth_complications", columnDefinition = "TEXT")
    private String birthComplications;

    @Column(name = "apgar_score", length = 20)
    private String apgarScore;

    @Column(name = "birth_order")
    private Integer birthOrder = 1;

    @Column(name = "is_alive")
    private Boolean isAlive = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum Gender { MALE, FEMALE }

    public Baby() {}

    // Getters and setters

    public Integer getBabyId() { return babyId; }
    public void setBabyId(Integer babyId) { this.babyId = babyId; }

    public Integer getPregnancyId() { return pregnancyId; }
    public void setPregnancyId(Integer pregnancyId) { this.pregnancyId = pregnancyId; }

    public Integer getMotherId() { return motherId; }
    public void setMotherId(Integer motherId) { this.motherId = motherId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    public Gender getGender() { return gender; }
    public void setGender(Gender gender) { this.gender = gender; }

    public Float getBirthWeight() { return birthWeight; }
    public void setBirthWeight(Float birthWeight) { this.birthWeight = birthWeight; }

    public Float getBirthHeight() { return birthHeight; }
    public void setBirthHeight(Float birthHeight) { this.birthHeight = birthHeight; }

    public String getBirthComplications() { return birthComplications; }
    public void setBirthComplications(String birthComplications) { this.birthComplications = birthComplications; }

    public String getApgarScore() { return apgarScore; }
    public void setApgarScore(String apgarScore) { this.apgarScore = apgarScore; }

    public Integer getBirthOrder() { return birthOrder; }
    public void setBirthOrder(Integer birthOrder) { this.birthOrder = birthOrder; }

    public Boolean getIsAlive() { return isAlive; }
    public void setIsAlive(Boolean isAlive) { this.isAlive = isAlive; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

}
