package com.moh.moh_backend.dto;

import com.moh.moh_backend.model.Baby;
import com.moh.moh_backend.model.Mother;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
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
    public List<BabyInfo> babies;

    public static class BabyInfo {
        public Integer babyId;
        public String name;
        public LocalDate dateOfBirth;
        public Integer ageMonths;
        public String gender;
        public Boolean isAlive;

        public static BabyInfo from(Baby b) {
            BabyInfo info = new BabyInfo();
            info.babyId = b.getBabyId();
            info.name = b.getName();
            info.dateOfBirth = b.getDateOfBirth();
            info.gender = b.getGender() != null ? b.getGender().name() : null;
            info.isAlive = b.getIsAlive();
            if (b.getDateOfBirth() != null) {
                info.ageMonths = Period.between(b.getDateOfBirth(), LocalDate.now()).getMonths()
                        + Period.between(b.getDateOfBirth(), LocalDate.now()).getYears() * 12;
            }
            return info;
        }
    }

    public static FamilyResponse from(Mother m, List<Baby> babies) {
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
        dto.isActive        = m.getActive();
        if (m.getPhmArea() != null) {
            dto.phmAreaName = m.getPhmArea().getAreaName();
        }
        dto.babies = babies.stream()
                .filter(b -> Boolean.TRUE.equals(b.getIsAlive()))
                .map(BabyInfo::from)
                .collect(Collectors.toList());
        return dto;
    }
}
