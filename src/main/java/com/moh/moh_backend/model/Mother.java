package com.moh.moh_backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "MOTHER")
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class Mother {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "mother_id")
    private Integer motherId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @ManyToOne
    @JoinColumn(name = "phm_area_id", nullable = false)
    private PhmArea phmArea;

    @Column(name = "address", columnDefinition = "TEXT")
    private String address;

    @Column(name = "nic", length = 20, nullable = false, unique = true)
    private String nic;

    @Column(name = "name", length = 50, nullable = false)
    private String name;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "occupation", length = 50)
    private String occupation;

    @Column(name = "contact_number", length = 20)
    private String contactNumber;

    @Column(name = "blood_group", length = 10)
    private String bloodGroup;

    @Column(name = "registration_date")
    private LocalDate registrationDate;

    @Column(name = "allergies", columnDefinition = "TEXT")
    private String allergies;

    @Column(name = "husband_name", length = 100)
    private String husbandName;

    @Column(name = "husband_nic", length = 20)
    private String husbandNic;

    @Column(name = "husband_dob")
    private LocalDate husbandDob;

    @Column(name = "husband_age")
    private Integer husbandAge;

    @Column(name = "husband_phone", length = 20)
    private String husbandPhone;

    @Column(name = "husband_email", length = 100)
    private String husbandEmail;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;


        public void setActive(Boolean active) {
        isActive = active;
    }

        public Boolean getActive() {
        return isActive;
    }


    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

}