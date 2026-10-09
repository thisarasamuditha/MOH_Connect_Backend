package com.moh.moh_backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "PHM_AREA")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PhmArea {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "phm_area_id")
    private Integer phmAreaId;

    @Column(name = "area_name", nullable = false, length = 255)
    private String areaName;

    @Column(name = "area_code", unique = true, nullable = false, length = 50)
    private String areaCode;


    public PhmArea(String areaName, String areaCode) {
        this.areaName = areaName;
        this.areaCode = areaCode;
    }

}
