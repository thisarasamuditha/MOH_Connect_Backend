package com.moh.moh_backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.Date;

public class MotherRegisterRequest {
    @NotBlank public String username;
    @NotBlank public String email;
    @NotBlank public String password;
    @NotBlank public String nic;
    @NotBlank public String name;
    @NotNull public Integer phmAreaId;
    public String address;
    public LocalDate dateOfBirth;
    public String occupation;
    public String contactNumber;
    public String bloodGroup;
    public LocalDate registrationDate;
}