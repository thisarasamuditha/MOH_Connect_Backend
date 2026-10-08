package com.moh.moh_backend.dto;

import com.moh.moh_backend.model.Baby;
import com.moh.moh_backend.model.BabyRecord;
import com.moh.moh_backend.model.Mother;
import com.moh.moh_backend.model.Pregnancy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;

public class BabyResponseDto {
    public Integer babyId;
    public String name;
    public LocalDate dateOfBirth;
    public Integer ageMonths;
    public Integer ageYears;
    public String gender;
    public Float birthWeight;
    public Float birthHeight;
    public String birthComplications;
    public String apgarScore;
    public Integer birthOrder;
    public Boolean isAlive;
    public String hospitalBorn;
    public String specialNotes;

    // Relational info
    public Integer motherId;
    public String motherName;
    public String motherNic;
    public Integer phmAreaId;
    public String phmAreaName;
    public Integer pregnancyId;
    public String pregnancyNumber;

    // Latest Vitals & Growth
    public Float currentWeight;
    public Float currentHeight;
    public Float latestBmi;
    public String latestTemperature;
    public String growthStatus; // NORMAL, UNDERWEIGHT, OVERWEIGHT, STUNTED, WASTED
    public Double weightForAgeZ;
    public String growthZone; // SEVERE_UNDERWEIGHT, MODERATE_UNDERWEIGHT, NORMAL, ABOVE_NORMAL
    public String growthZoneLabel;
    public LocalDate growthMeasuredOn;
    public String latestFindings;
    public String latestRecommendations;
    public LocalDate latestRecordDate;
    public LocalDate nextVaccineDate;
    public String vaccineStatus; // up-to-date, delayed, incomplete

    // Risk Assessment
    public String riskLevel; // LOW, MEDIUM, HIGH
    public List<String> riskReasons = new ArrayList<>();

    public LocalDateTime createdAt;
    public LocalDateTime updatedAt;

    public static BabyResponseDto from(Baby b, Mother mother, Pregnancy pregnancy, BabyRecord latestRecord) {
        BabyResponseDto dto = new BabyResponseDto();
        dto.babyId = b.getBabyId();
        dto.name = b.getName();
        dto.dateOfBirth = b.getDateOfBirth();
        dto.gender = b.getGender() != null ? b.getGender().name() : null;
        dto.birthWeight = b.getBirthWeight();
        dto.birthHeight = b.getBirthHeight();
        dto.birthComplications = b.getBirthComplications();
        dto.apgarScore = b.getApgarScore();
        dto.birthOrder = b.getBirthOrder();
        dto.isAlive = b.getIsAlive();
        dto.hospitalBorn = b.getHospitalBorn();
        dto.specialNotes = b.getSpecialNotes();
        dto.motherId = b.getMotherId();
        dto.pregnancyId = b.getPregnancyId();
        dto.createdAt = b.getCreatedAt();
        dto.updatedAt = b.getUpdatedAt();

        if (b.getDateOfBirth() != null) {
            Period p = Period.between(b.getDateOfBirth(), LocalDate.now());
            dto.ageYears = p.getYears();
            dto.ageMonths = p.getYears() * 12 + p.getMonths();
        }

        if (mother != null) {
            dto.motherName = mother.getName();
            dto.motherNic = mother.getNic();
            if (mother.getPhmArea() != null) {
                dto.phmAreaId = mother.getPhmArea().getPhmAreaId();
                dto.phmAreaName = mother.getPhmArea().getAreaName();
            }
        }

        if (pregnancy != null) {
            dto.pregnancyNumber = pregnancy.getPregnancyNumber();
        }

        // Set vitals from latest record or fallback to birth values
        if (latestRecord != null) {
            dto.currentWeight = latestRecord.getWeight() != null ? latestRecord.getWeight() : b.getBirthWeight();
            dto.currentHeight = latestRecord.getHeight() != null ? latestRecord.getHeight() : b.getBirthHeight();
            dto.latestBmi = latestRecord.getBmi();
            dto.latestTemperature = latestRecord.getTemperature();
            dto.growthStatus = latestRecord.getGrowthStatus() != null ? latestRecord.getGrowthStatus().name() : "NORMAL";
            dto.latestFindings = latestRecord.getFindings();
            dto.latestRecommendations = latestRecord.getRecommendations();
            dto.latestRecordDate = latestRecord.getRecordDate();
            dto.nextVaccineDate = latestRecord.getNextVisitDate();
        } else {
            dto.currentWeight = b.getBirthWeight();
            dto.currentHeight = b.getBirthHeight();
            dto.growthStatus = "NORMAL";
        }

        // Weight-for-age (WHO, sex-specific) evaluated at the date the weight was measured:
        // the latest visit, otherwise the birth weight at birth.
        com.moh.moh_backend.model.Baby.Gender sex = b.getGender();
        if (latestRecord != null && latestRecord.getWeight() != null) {
            dto.growthMeasuredOn = latestRecord.getRecordDate();
            dto.weightForAgeZ = com.moh.moh_backend.util.WhoWeightForAge.zScore(sex, b.getDateOfBirth(), latestRecord.getRecordDate(), latestRecord.getWeight());
        } else if (b.getBirthWeight() != null) {
            dto.growthMeasuredOn = b.getDateOfBirth();
            dto.weightForAgeZ = com.moh.moh_backend.util.WhoWeightForAge.zScore(sex, b.getDateOfBirth(), b.getDateOfBirth(), b.getBirthWeight());
        }
        var zone = com.moh.moh_backend.util.WhoWeightForAge.zone(dto.weightForAgeZ);
        if (zone != null) {
            dto.growthZone = zone.name();
            dto.growthZoneLabel = com.moh.moh_backend.util.WhoWeightForAge.label(zone);
            // STUNTED/WASTED are length-based assessments entered by staff; keep them.
            if (!"STUNTED".equalsIgnoreCase(dto.growthStatus) && !"WASTED".equalsIgnoreCase(dto.growthStatus)) {
                dto.growthStatus = com.moh.moh_backend.util.WhoWeightForAge.toGrowthStatus(zone).name();
            }
        }

        // Determine Risk Level & Reasons
        dto.riskLevel = "LOW";

        if (b.getSpecialNotes() != null && b.getSpecialNotes().toUpperCase().contains("HIGH RISK")) {
            dto.riskLevel = "HIGH";
            dto.riskReasons.add(b.getSpecialNotes());
        }

        if (latestRecord != null) {
            if ("UNDERWEIGHT".equalsIgnoreCase(dto.growthStatus) || "WASTED".equalsIgnoreCase(dto.growthStatus)) {
                dto.riskLevel = "HIGH";
                dto.riskReasons.add("Growth Status: " + (dto.growthZoneLabel != null ? dto.growthZoneLabel : dto.growthStatus));
            } else if ("OVERWEIGHT".equalsIgnoreCase(dto.growthStatus) || "STUNTED".equalsIgnoreCase(dto.growthStatus)) {
                if (!"HIGH".equals(dto.riskLevel)) dto.riskLevel = "MEDIUM";
                dto.riskReasons.add("Growth Status: " + dto.growthStatus);
            }

            if (latestRecord.getTemperature() != null) {
                try {
                    double temp = Double.parseDouble(latestRecord.getTemperature().trim());
                    if (temp >= 38.0) {
                        dto.riskLevel = "HIGH";
                        dto.riskReasons.add(String.format("High Fever Alert: %.1f°C", temp));
                    } else if (temp >= 37.6) {
                        if (!"HIGH".equals(dto.riskLevel)) dto.riskLevel = "MEDIUM";
                        dto.riskReasons.add(String.format("Low-grade fever: %.1f°C", temp));
                    }
                } catch (NumberFormatException ignored) {}
            }
        }

        if (b.getBirthComplications() != null && !b.getBirthComplications().isBlank() && !"None".equalsIgnoreCase(b.getBirthComplications())) {
            if (!"HIGH".equals(dto.riskLevel)) dto.riskLevel = "MEDIUM";
            dto.riskReasons.add("Birth Complication: " + b.getBirthComplications());
        }

        dto.vaccineStatus = "up-to-date";
        return dto;
    }
}
