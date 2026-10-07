package com.moh.moh_backend.dto;

import com.moh.moh_backend.model.Baby;
import com.moh.moh_backend.model.BabyRecord;
import com.moh.moh_backend.model.Mother;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class FamilyResponse {
    public Integer motherId;
    public String name;
    public String nic;
    public String contactNumber;
    public String address;
    public String occupation;
    public String bloodGroup;
    public LocalDate dateOfBirth;
    public LocalDate registrationDate;
    public Boolean isActive;
    public String phmAreaName;
    public String allergies;
    public String husbandName;
    public String husbandNic;
    public LocalDate husbandDob;
    public Integer husbandAge;
    public String husbandPhone;
    public String husbandEmail;
    public List<BabyInfo> babies;

    public static class BabyInfo {
        public Integer babyId;
        public String name;
        public LocalDate dateOfBirth;
        public Integer ageMonths;
        public String gender;
        public Boolean isAlive;
        public String hospitalBorn;
        public String specialNotes;
        public Float birthWeight;
        public Float birthHeight;
        public Float currentWeight;
        public Float currentHeight;
        public String latestTemperature;
        public String growthStatus;
        public String latestFindings;
        public String riskLevel;

        public static BabyInfo from(Baby b, BabyRecord latestRecord) {
            BabyInfo info = new BabyInfo();
            info.babyId = b.getBabyId();
            info.name = b.getName();
            info.dateOfBirth = b.getDateOfBirth();
            info.gender = b.getGender() != null ? b.getGender().name() : null;
            info.isAlive = b.getIsAlive();
            info.hospitalBorn = b.getHospitalBorn();
            info.specialNotes = b.getSpecialNotes();
            info.birthWeight = b.getBirthWeight();
            info.birthHeight = b.getBirthHeight();
            if (b.getDateOfBirth() != null) {
                info.ageMonths = Period.between(b.getDateOfBirth(), LocalDate.now()).getMonths()
                        + Period.between(b.getDateOfBirth(), LocalDate.now()).getYears() * 12;
            }

            if (latestRecord != null) {
                info.currentWeight = latestRecord.getWeight() != null ? latestRecord.getWeight() : b.getBirthWeight();
                info.currentHeight = latestRecord.getHeight() != null ? latestRecord.getHeight() : b.getBirthHeight();
                info.latestTemperature = latestRecord.getTemperature();
                info.growthStatus = latestRecord.getGrowthStatus() != null ? latestRecord.getGrowthStatus().name() : "NORMAL";
                info.latestFindings = latestRecord.getFindings();
            } else {
                info.currentWeight = b.getBirthWeight();
                info.currentHeight = b.getBirthHeight();
                info.growthStatus = "NORMAL";
            }

            // Risk flag
            if ((b.getSpecialNotes() != null && b.getSpecialNotes().toUpperCase().contains("HIGH RISK"))
                    || "UNDERWEIGHT".equalsIgnoreCase(info.growthStatus)
                    || "WASTED".equalsIgnoreCase(info.growthStatus)) {
                info.riskLevel = "HIGH";
            } else {
                info.riskLevel = "LOW";
            }

            return info;
        }

        public static BabyInfo from(Baby b) {
            return from(b, null);
        }
    }

    public static FamilyResponse from(Mother m, List<Baby> babies, Map<Integer, BabyRecord> latestRecordsMap) {
        FamilyResponse dto = new FamilyResponse();
        dto.motherId        = m.getMotherId();
        dto.name            = m.getName();
        dto.nic             = m.getNic();
        dto.contactNumber   = m.getContactNumber();
        dto.address         = m.getAddress();
        dto.occupation      = m.getOccupation();
        dto.bloodGroup      = m.getBloodGroup();
        dto.dateOfBirth     = m.getDateOfBirth();
        dto.registrationDate = m.getRegistrationDate();
        dto.allergies       = m.getAllergies();
        dto.husbandName     = m.getHusbandName();
        dto.husbandNic      = m.getHusbandNic();
        dto.husbandDob      = m.getHusbandDob();
        dto.husbandAge      = m.getHusbandAge();
        dto.husbandPhone    = m.getHusbandPhone();
        dto.husbandEmail    = m.getHusbandEmail();
        dto.isActive        = m.getActive();
        if (m.getPhmArea() != null) {
            dto.phmAreaName = m.getPhmArea().getAreaName();
        }
        dto.babies = babies.stream()
                .filter(b -> Boolean.TRUE.equals(b.getIsAlive()))
                .map(b -> BabyInfo.from(b, latestRecordsMap != null ? latestRecordsMap.get(b.getBabyId()) : null))
                .collect(Collectors.toList());
        return dto;
    }

    public static FamilyResponse from(Mother m, List<Baby> babies) {
        return from(m, babies, null);
    }
}
