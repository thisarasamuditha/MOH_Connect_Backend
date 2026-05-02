package com.moh.moh_backend.service;

import com.moh.moh_backend.dto.FamilyResponse;
import com.moh.moh_backend.dto.MotherRegisterRequest;
import com.moh.moh_backend.dto.MotherResponse;
import com.moh.moh_backend.model.Baby;
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
import com.moh.moh_backend.util.CredentialGenerator;
import com.moh.moh_backend.util.EmailService;
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
    private final EmailService emailService;

    public MotherService(UserRepository userRepo, MotherRepository motherRepo,
                         MidwifeRepository midwifeRepo, PhmAreaRepository phmAreaRepo,
                         PasswordHashService hashService, BabyRepository babyRepo, EmailService emailService) {
        this.userRepo = userRepo;
        this.motherRepo = motherRepo;
        this.midwifeRepo = midwifeRepo;
        this.phmAreaRepo = phmAreaRepo;
        this.hashService = hashService;
        this.babyRepo = babyRepo;
        this.emailService = emailService;
    }

    @Transactional
    public void registerMother(MotherRegisterRequest req, Integer midwifeUserId) {
        // Validate required fields
        if (req.email == null || req.email.trim().isEmpty()) {
            throw new IllegalArgumentException("Mother's email is required");
        }
        if (req.name == null || req.name.trim().isEmpty()) {
            throw new IllegalArgumentException("Mother's name is required");
        }
        if (req.nic == null || req.nic.trim().isEmpty()) {
            throw new IllegalArgumentException("Mother's NIC is required");
        }
        
        // Auto-generate username and password if not provided by midwife
        String username = (req.username != null && !req.username.trim().isEmpty()) ? 
                req.username : CredentialGenerator.generateUsername();
        String password = (req.password != null && !req.password.trim().isEmpty()) ? 
                req.password : CredentialGenerator.generatePassword();
        
        System.out.println("Registering mother: " + req.name + " by midwife user ID: " + midwifeUserId);
        System.out.println("Auto-generated username: " + username);
        
        // Load midwife and use their PHM area
        Midwife midwife = midwifeRepo.findByUser_UserId(midwifeUserId)
                .orElseThrow(() -> new IllegalArgumentException("Midwife not found"));
        
        System.out.println("Midwife found: " + midwife.getName());
        
        if (midwife.getPhmArea() == null) {
            throw new IllegalStateException("Midwife '" + midwife.getName() + "' has no PHM area assigned");
        }
        
        // Use midwife's PHM area automatically (ignore request phmAreaId)
        Integer phmAreaId = midwife.getPhmArea().getPhmAreaId();
        System.out.println("Mother will be registered in PHM Area: " + phmAreaId + " (Midwife's area)");

        // Uniqueness checks
        if (motherRepo.existsByNic(req.nic)) throw new IllegalArgumentException("NIC already registered");
        if (userRepo.existsByUsername(username)) throw new IllegalArgumentException("Username already exists");
        if (userRepo.existsByEmail(req.email)) throw new IllegalArgumentException("Email already registered");

        // Create USER (role MOTHER)
        User user = new User();
        user.setUsername(username);
        user.setEmail(req.email);
        user.setPasswordHash(hashService.hashSha256(password));
        user.setRole(UserRole.MOTHER);
        user.setIsActive(true);
        userRepo.save(user);

        // Validate PHM area (using midwife's area)
        PhmArea phmArea = phmAreaRepo.findById(phmAreaId)
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

        // Send credentials to mother's email
        emailService.sendMotherCredentials(req.email, username, password, req.name);
    }

    @Transactional(readOnly = true)
    public List<MotherResponse> getMothersByMidwife(Integer midwifeUserId) {
        Midwife midwife = midwifeRepo.findByUser_UserId(midwifeUserId)
                .orElseThrow(() -> new IllegalArgumentException("Midwife not found for userId: " + midwifeUserId));
        if (midwife.getPhmArea() == null) {
            throw new IllegalStateException("Midwife has no PHM area assigned");
        }
        Integer phmAreaId = midwife.getPhmArea().getPhmAreaId();
        return motherRepo.findByPhmArea_PhmAreaId(phmAreaId)
                .stream()
                .map(MotherResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<FamilyResponse> getFamiliesForMidwife(Integer midwifeUserId) {
        System.out.println("Fetching families for midwife user ID: " + midwifeUserId);
        Midwife midwife = midwifeRepo.findByUser_UserId(midwifeUserId)
                .orElseThrow(() -> new IllegalArgumentException("Midwife not found for userId: " + midwifeUserId));
        if (midwife.getPhmArea() == null) {
            throw new IllegalStateException("Midwife has no PHM area assigned");
        }
        Integer phmAreaId = midwife.getPhmArea().getPhmAreaId();
        System.out.println("Fetching mothers for PHM Area ID: " + phmAreaId);
        List<Mother> mothers = motherRepo.findByPhmArea_PhmAreaId(phmAreaId);
        System.out.println("Found " + mothers.size() + " mothers");
        return mothers.stream()
                .map(mother -> {
                    System.out.println("Processing mother: " + mother.getName() + " (ID: " + mother.getMotherId() + ")");
                    List<Baby> babies = babyRepo.findByMotherId(mother.getMotherId());
                    System.out.println("Found " + babies.size() + " babies for mother");
                    return FamilyResponse.from(mother, babies);
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public Mother updateMother(Integer motherId, Integer midwifeUserId, java.util.Map<String, String> updateData) {
        // Verify midwife exists
        Midwife midwife = midwifeRepo.findByUser_UserId(midwifeUserId)
                .orElseThrow(() -> new IllegalArgumentException("Midwife not found"));

        // Get mother
        Mother mother = motherRepo.findById(motherId)
                .orElseThrow(() -> new IllegalArgumentException("Mother not found with ID: " + motherId));

        // Verify midwife can only update mothers in their PHM area
        if (!mother.getPhmArea().getPhmAreaId().equals(midwife.getPhmArea().getPhmAreaId())) {
            throw new IllegalStateException("Midwife can only update mothers in their PHM area");
        }

        // Update fields if provided
        if (updateData.containsKey("name") && updateData.get("name") != null) {
            mother.setName(updateData.get("name"));
        }
        if (updateData.containsKey("contactNumber") && updateData.get("contactNumber") != null) {
            mother.setContactNumber(updateData.get("contactNumber"));
        }
        if (updateData.containsKey("address") && updateData.get("address") != null) {
            mother.setAddress(updateData.get("address"));
        }
        if (updateData.containsKey("bloodGroup") && updateData.get("bloodGroup") != null) {
            mother.setBloodGroup(updateData.get("bloodGroup"));
        }
        if (updateData.containsKey("occupation") && updateData.get("occupation") != null) {
            mother.setOccupation(updateData.get("occupation"));
        }

        return motherRepo.save(mother);
    }

    @Transactional
    public void deleteMother(Integer motherId, Integer midwifeUserId) {
        // Verify midwife exists
        Midwife midwife = midwifeRepo.findByUser_UserId(midwifeUserId)
                .orElseThrow(() -> new IllegalArgumentException("Midwife not found"));

        // Get mother
        Mother mother = motherRepo.findById(motherId)
                .orElseThrow(() -> new IllegalArgumentException("Mother not found with ID: " + motherId));

        // Verify midwife can only delete mothers in their PHM area
        if (!mother.getPhmArea().getPhmAreaId().equals(midwife.getPhmArea().getPhmAreaId())) {
            throw new IllegalStateException("Midwife can only delete mothers in their PHM area");
        }

        // Soft delete: mark as inactive instead of hard delete
        mother.setActive(false);
        motherRepo.save(mother);
    }
}