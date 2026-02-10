package com.moh.moh_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BabyVaccinationCreateDto {
    private Integer babyId;
    private Integer midwifeId;
    private Integer scheduleId;
    private LocalDate vaccinationDate;
    private String batchNumber;
    private String manufacturer;
    private LocalDate nextDoseDate;
    private String adverseReaction;
}
