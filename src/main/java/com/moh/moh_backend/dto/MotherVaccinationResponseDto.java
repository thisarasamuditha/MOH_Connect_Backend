package com.moh.moh_backend.dto;

import com.moh.moh_backend.model.VaccineSchedule;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MotherVaccinationResponseDto {
    private Integer vaccinationId;
    private Integer pregnancyId;
    private String motherName;
    private Integer midwifeId;
    private String midwifeName;
    private VaccineSchedule schedule;
    private LocalDate vaccinationDate;
    private String batchNumber;
    private String manufacturer;
    private LocalDate nextDoseDate;
    private String adverseReaction;
    private LocalDateTime createdAt;
}
