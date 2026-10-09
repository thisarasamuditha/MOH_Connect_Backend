package com.moh.moh_backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import com.moh.moh_backend.Enum.Gender;

import java.time.LocalDate;
import java.time.LocalDateTime;
@Data
@Entity
@Table(name = "BABY")
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder

public class Baby {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "baby_id")
    private Integer babyId;

    @Column(name = "pregnancy_id")
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

    @Column(name = "hospital_born", length = 255)
    private String hospitalBorn;

    @Column(name = "special_notes", columnDefinition = "TEXT")
    private String specialNotes;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

   // public Baby() {}


}
