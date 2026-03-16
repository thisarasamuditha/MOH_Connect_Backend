package com.moh.moh_backend.service;

import com.moh.moh_backend.dto.FamilyResponse;
import com.moh.moh_backend.dto.MotherRegisterRequest;
import com.moh.moh_backend.dto.MotherResponse;
import com.moh.moh_backend.dto.MotherUpdateRequest;
import com.moh.moh_backend.model.Midwife;
import com.moh.moh_backend.model.Mother;
import com.moh.moh_backend.model.PhmArea;
import com.moh.moh_backend.model.User;
import com.moh.moh_backend.model.UserRole;
import com.moh.moh_backend.repository.BabyRepository;
import com.moh.moh_backend.repository.MidwifeRepository;
import com.moh.moh_backend.repository.MotherRepository;
import com.moh.moh_backend.repository.PhmAreaRepository;
import com.moh.moh_backend.repository.UserRepository;
import com.moh.moh_backend.util.PasswordHashService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MotherService {
    private final UserRepository userRepo;
    private final MotherRepository motherRepo;
    private final MidwifeRepository midwifeRepo;
    private final PhmAreaRepository phmAreaRepo;
    private final PasswordHashService hashService;
    private final BabyRepository babyRepo;

    public MotherService(UserRepository userRepo, MotherRepository motherRepo,
                         MidwifeRepository midwifeRepo, PhmAreaRepository phmAreaRepo,
                         PasswordHashService hashService, BabyRepository babyRepo) {
        this.userRepo = userRepo;
        this.motherRepo = motherRepo;
        this.midwifeRepo = midwifeRepo;
        this.phmAreaRepo = phmAreaRepo;
        this.hashService = hashService;
        this.babyRepo = babyRepo;
    }

    @Transactional
    public void registerMother(MotherRegisterRequest req, Integer midwifeUserId) {
        System.out.println("Registering mother: " + req.username + " by midwife user ID: " + midwifeUserId);
        // Load midwife — mother is automatically assigned to midwife's PHM area
        Midwife midwife = midwifeRepo.findByUser_UserId(midwifeUserId)
                .orElseThrow(() -> new IllegalArgumentException("Midwife not found"));
        PhmArea phmArea = midwife.getPhmArea();
        if (phmArea == null) {
            throw new IllegalStateException("Midwife has no PHM area assigned");
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
        mother.setFatherName(req.fatherName);
        mother.setFatherNic(req.fatherNic);
        mother.setFatherDateOfBirth(req.fatherDateOfBirth);
        mother.setFatherContactNumber(req.fatherContactNumber);
        mother.setFatherEmail(req.fatherEmail);
        mother.setBloodGroup(req.bloodGroup);
        mother.setRegistrationDate(req.registrationDate);
        mother.setActive(true);
        motherRepo.save(mother);
    }

    @Transactional(readOnly = true)
    public List<MotherResponse> getMothersByMidwife(Integer midwifeUserId) {
        Midwife midwife = midwifeRepo.findByUser_UserId(midwifeUserId)
                .orElseThrow(() -> new IllegalArgumentException("Midwife not found for userId: " + midwifeUserId));
        if (midwife.getPhmArea() == null) {
            throw new IllegalStateException("Midwife has no PHM area assigned");
        }
        Integer phmAreaId = midwife.getPhmArea().getPhmAreaId();
        return motherRepo.findByPhmArea_PhmAreaIdAndIsActiveTrue(phmAreaId)
                .stream()
                .map(MotherResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<FamilyResponse> getFamiliesForMidwife(Integer midwifeUserId) {
        Midwife midwife = midwifeRepo.findByUser_UserId(midwifeUserId)
                .orElseThrow(() -> new IllegalArgumentException("Midwife not found for userId: " + midwifeUserId));
        if (midwife.getPhmArea() == null) {
            throw new IllegalStateException("Midwife has no PHM area assigned");
        }
        Integer phmAreaId = midwife.getPhmArea().getPhmAreaId();
        return motherRepo.findByPhmArea_PhmAreaIdAndIsActiveTrue(phmAreaId)
                .stream()
                .map(mother -> FamilyResponse.from(mother, babyRepo.findByMotherId(mother.getMotherId())))
                .collect(Collectors.toList());
    }

    @Transactional
    public FamilyResponse updateFamilyForMidwife(Integer motherId, MotherUpdateRequest req, Integer midwifeUserId) {
        Mother mother = getMotherForMidwife(motherId, midwifeUserId);

        if (req.nic != null) {
            String nic = req.nic.trim();
            if (nic.isEmpty()) {
                throw new IllegalArgumentException("NIC cannot be empty");
            }
            if (!nic.equals(mother.getNic()) && motherRepo.existsByNic(nic)) {
                throw new IllegalArgumentException("NIC already registered");
            }
            mother.setNic(nic);
        }

        if (req.name != null) {
            String name = req.name.trim();
            if (name.isEmpty()) {
                throw new IllegalArgumentException("Mother name cannot be empty");
            }
            mother.setName(name);
        }

        if (req.dateOfBirth != null) mother.setDateOfBirth(req.dateOfBirth);
        if (req.occupation != null) mother.setOccupation(normalize(req.occupation));
        if (req.contactNumber != null) mother.setContactNumber(normalize(req.contactNumber));
        if (req.bloodGroup != null) mother.setBloodGroup(normalize(req.bloodGroup));
        if (req.address != null) mother.setAddress(normalize(req.address));

        if (req.fatherName != null) mother.setFatherName(normalize(req.fatherName));
        if (req.fatherNic != null) mother.setFatherNic(normalize(req.fatherNic));
        if (req.fatherDateOfBirth != null) mother.setFatherDateOfBirth(req.fatherDateOfBirth);
        if (req.fatherContactNumber != null) mother.setFatherContactNumber(normalize(req.fatherContactNumber));
        if (req.fatherEmail != null) mother.setFatherEmail(normalize(req.fatherEmail));

        Mother saved = motherRepo.save(mother);
        return FamilyResponse.from(saved, babyRepo.findByMotherId(saved.getMotherId()));
    }

    @Transactional
    public void deleteFamilyForMidwife(Integer motherId, Integer midwifeUserId) {
        Mother mother = getMotherForMidwife(motherId, midwifeUserId);
        mother.setActive(false);
        motherRepo.save(mother);

        User user = mother.getUser();
        if (user != null) {
            user.setIsActive(false);
            userRepo.save(user);
        }
    }

    private Mother getMotherForMidwife(Integer motherId, Integer midwifeUserId) {
        Midwife midwife = midwifeRepo.findByUser_UserId(midwifeUserId)
                .orElseThrow(() -> new IllegalArgumentException("Midwife not found"));
        if (midwife.getPhmArea() == null) {
            throw new IllegalStateException("Midwife has no PHM area assigned");
        }

        Integer phmAreaId = midwife.getPhmArea().getPhmAreaId();
        return motherRepo.findByMotherIdAndPhmArea_PhmAreaId(motherId, phmAreaId)
                .orElseThrow(() -> new IllegalArgumentException("Family not found in your PHM area"));
    }

    private String normalize(String value) {
        String trimmed = value == null ? null : value.trim();
        return (trimmed == null || trimmed.isEmpty()) ? null : trimmed;
    }
}