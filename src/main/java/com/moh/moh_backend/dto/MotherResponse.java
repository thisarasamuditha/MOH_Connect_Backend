package com.moh.moh_backend.dto;

import com.moh.moh_backend.model.Mother;

import java.time.LocalDate;

public class MotherResponse {
    public Integer motherId;
    public String name;
    public String nic;
    public String contactNumber;
    public String address;
    public String occupation;
    public String bloodGroup;
    public LocalDate dateOfBirth;
    public String fatherName;
    public String fatherNic;
    public LocalDate fatherDateOfBirth;
    public String fatherContactNumber;
    public String fatherEmail;
    public LocalDate registrationDate;
    public Boolean isActive;
    public Integer phmAreaId;
    public String phmAreaName;

    public static MotherResponse from(Mother m) {
        MotherResponse dto = new MotherResponse();
        dto.motherId       = m.getMotherId();
        dto.name           = m.getName();
        dto.nic            = m.getNic();
        dto.contactNumber  = m.getContactNumber();
        dto.address        = m.getAddress();
        dto.occupation     = m.getOccupation();
        dto.bloodGroup     = m.getBloodGroup();
        dto.dateOfBirth    = m.getDateOfBirth();
        dto.fatherName     = m.getFatherName();
        dto.fatherNic      = m.getFatherNic();
        dto.fatherDateOfBirth = m.getFatherDateOfBirth();
        dto.fatherContactNumber = m.getFatherContactNumber();
        dto.fatherEmail    = m.getFatherEmail();
        dto.registrationDate = m.getRegistrationDate();
        dto.isActive       = m.getActive();
        if (m.getPhmArea() != null) {
            dto.phmAreaId   = m.getPhmArea().getPhmAreaId();
            dto.phmAreaName = m.getPhmArea().getAreaName();
        }
        return dto;
    }
}
