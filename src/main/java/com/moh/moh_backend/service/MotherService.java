package com.moh.moh_backend.service;

import com.moh.moh_backend.dto.MotherRegisterRequest;
import com.moh.moh_backend.model.*;
import com.moh.moh_backend.repository.*;
import com.moh.moh_backend.util.PasswordHashService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MotherService {
    private final UserRepository userRepo;
    private final MotherRepository motherRepo;
    private final PhmAreaRepository phmAreaRepo;
    private final PasswordHashService hashService;

    public MotherService(UserRepository userRepo, MotherRepository motherRepo, PhmAreaRepository phmAreaRepo, PasswordHashService hashService) {
        this.userRepo = userRepo;
        this.motherRepo = motherRepo;
        this.phmAreaRepo = phmAreaRepo;
        this.hashService = hashService;
    }

    @Transactional
    public void registerMother(MotherRegisterRequest req) {
        if (userRepo.existsByEmail(req.email)) throw new IllegalArgumentException("Email exists");
        if (motherRepo.existsByNic(req.nic)) throw new IllegalArgumentException("NIC exists");

        User user = new User();
        user.setUsername(req.username);
        user.setEmail(req.email);
        user.setPasswordHash(hashService.hashSha256(req.password));
        user.setRole(UserRole.MOTHER);
        user.setIsActive(true);
        userRepo.save(user);

        PhmArea phmArea = phmAreaRepo.findById(req.phmAreaId)
                .orElseThrow(() -> new IllegalArgumentException("PHM Area not found"));

        Mother mother = new Mother();
        mother.setUser(user);
        mother.setPhmArea(phmArea);
        mother.setNic(req.nic);
        mother.setName(req.name);
        mother.setDateOfBirth(req.dateOfBirth);
        mother.setOccupation(req.occupation);
        mother.setContactNumber(req.contactNumber);
        mother.setBloodGroup(req.bloodGroup);
        mother.setAddress(req.address);
        mother.setRegistrationDate(req.registrationDate);
        mother.setActive(true);
        motherRepo.save(mother);
    }
}