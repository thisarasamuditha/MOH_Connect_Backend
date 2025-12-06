package com.moh.moh_backend.service;

import com.moh.moh_backend.dto.MotherRegisterRequest;
import com.moh.moh_backend.model.Midwife;
import com.moh.moh_backend.model.Mother;
import com.moh.moh_backend.model.PhmArea;
import com.moh.moh_backend.model.User;
import com.moh.moh_backend.model.UserRole;
import com.moh.moh_backend.repository.MidwifeRepository;
import com.moh.moh_backend.repository.MotherRepository;
import com.moh.moh_backend.repository.PhmAreaRepository;
import com.moh.moh_backend.repository.UserRepository;
import com.moh.moh_backend.util.PasswordHashService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MotherService {
    private final UserRepository userRepo;
    private final MotherRepository motherRepo;
    private final MidwifeRepository midwifeRepo;
    private final PhmAreaRepository phmAreaRepo;
    private final PasswordHashService hashService;

    public MotherService(UserRepository userRepo, MotherRepository motherRepo,
                         MidwifeRepository midwifeRepo, PhmAreaRepository phmAreaRepo,
                         PasswordHashService hashService) {
        this.userRepo = userRepo;
        this.motherRepo = motherRepo;
        this.midwifeRepo = midwifeRepo;
        this.phmAreaRepo = phmAreaRepo;
        this.hashService = hashService;
    }

    @Transactional
    public void registerMother(MotherRegisterRequest req, Integer midwifeUserId) {
        System.out.println("Registering mother: " + req.username + " by midwife user ID: " + midwifeUserId);
        // Load midwife and enforce PHM area ownership
        Midwife midwife = midwifeRepo.findByUser_UserId(midwifeUserId)
                .orElseThrow(() -> new IllegalArgumentException("Midwife not found"));
        if (midwife.getPhmArea() == null || !midwife.getPhmArea().getPhmAreaId().equals(req.phmAreaId)) {
            throw new IllegalStateException("Midwife not assigned to this PHM area");
        }

        // Uniqueness checks
        if (motherRepo.existsByNic(req.nic)) throw new IllegalArgumentException("NIC already registered");
        if (userRepo.existsByUsername(req.username)) throw new IllegalArgumentException("Username exists");
        if (userRepo.existsByEmail(req.email)) throw new IllegalArgumentException("Email exists");

        // Create USER (role MOTHER)
        User user = new User();
        user.setUsername(req.username);
        user.setEmail(req.email);
        user.setPasswordHash(hashService.hashSha256(req.password)); // SHA-256 as requested
        user.setRole(UserRole.MOTHER);
        user.setIsActive(true);
        userRepo.save(user);

        // Validate PHM area
        PhmArea phmArea = phmAreaRepo.findById(req.phmAreaId)
                .orElseThrow(() -> new IllegalArgumentException("PHM Area not found"));

        // Create MOTHER
        Mother mother = new Mother();
        mother.setUser(user);
        mother.setPhmArea(phmArea);
        mother.setAddress(req.address);
        mother.setNic(req.nic);
        mother.setName(req.name);
        mother.setDateOfBirth(req.dateOfBirth);
        mother.setOccupation(req.occupation);
        mother.setContactNumber(req.contactNumber);
        mother.setBloodGroup(req.bloodGroup);
        mother.setRegistrationDate(req.registrationDate);
        mother.setActive(true);
        motherRepo.save(mother);
    }
}