package com.moh.moh_backend.dto;

import java.time.LocalDate;

public class MotherUpdateRequest {
    public String name;
    public String nic;
    public LocalDate dateOfBirth;
    public String occupation;
    public String contactNumber;
    public String bloodGroup;
    public String address;

    public String fatherName;
    public String fatherNic;
    public LocalDate fatherDateOfBirth;
    public String fatherContactNumber;
    public String fatherEmail;
}
