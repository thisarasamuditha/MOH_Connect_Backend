package com.moh.moh_backend.service;

import com.moh.moh_backend.dto.AuthDtos;
import com.moh.moh_backend.model.*;
import com.moh.moh_backend.repository.DoctorRepository;
import com.moh.moh_backend.repository.MidwifeRepository;
import com.moh.moh_backend.repository.MotherRepository;
import com.moh.moh_backend.repository.PhmAreaRepository;
import com.moh.moh_backend.repository.UserRepository;
import com.moh.moh_backend.util.JwtService;
import com.moh.moh_backend.util.PasswordHashService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Handles registration and login flows without Spring Security.
 * - Registration stores SHA-256 hashed password
 * - Login verifies SHA-256 hash
 * - Generates JWT containing username and role
 */
@Service
public class AuthService {
    private final UserRepository userRepo;
    private final PasswordHashService hashService;
    private final JwtService jwtService;
    private final DoctorRepository doctorRepo;
    private final MidwifeRepository midwifeRepo;
    private final MotherRepository motherRepo;
    private final PhmAreaRepository phmAreaRepo;

    public AuthService(UserRepository userRepo, PasswordHashService hashService, JwtService jwtService, DoctorRepository doctorRepo, MidwifeRepository midwifeRepo, MotherRepository motherRepo, PhmAreaRepository phmAreaRepo) {
        this.userRepo = userRepo;
        this.hashService = hashService;
        this.jwtService = jwtService;
        this.doctorRepo = doctorRepo;
        this.midwifeRepo = midwifeRepo;
        this.motherRepo = motherRepo;
        this.phmAreaRepo = phmAreaRepo;
    }

    @Transactional
    public AuthDtos.AuthResponse register(AuthDtos.RegisterRequest req) {
        if (userRepo.existsByUsername(req.username)) {
            throw new IllegalArgumentException("Username already taken");

        }
        if (userRepo.existsByEmail(req.email)) {
            throw new IllegalArgumentException("Email already registered");
        }

        User user = new User();
        user.setUsername(req.username);
        user.setEmail(req.email);
        user.setPasswordHash(hashService.hashSha256(req.password));
        user.setRole(UserRole.valueOf(req.role));
        user.setIsActive(true);
        userRepo.save(user);

        // Doctor registration: fill DOCTOR table
        if ("DOCTOR".equalsIgnoreCase(req.role) && req.doctorDetails != null) {
            if (doctorRepo.existsByLicenseNumber(req.doctorDetails.licenseNumber))
                throw new IllegalArgumentException("License number exists");
            Doctor doctor = new Doctor();
            doctor.setUser(user); // Set the User entity, not userId
            doctor.setName(req.doctorDetails.name);
            doctor.setSpecialization(req.doctorDetails.specialization);
            doctor.setContactNumber(req.doctorDetails.contactNumber);
            doctor.setEmail(req.doctorDetails.email);
            doctor.setLicenseNumber(req.doctorDetails.licenseNumber);
            doctorRepo.save(doctor);
        }

        if ("MIDWIFE".equalsIgnoreCase(req.role) && req.midwifeDetails != null) {
            Midwife midwife = new Midwife();
            midwife.setUser(user); // Set the User entity, not userId
            midwife.setName(req.midwifeDetails.name);

            // Fetch existing PHM_AREA by ID
            PhmArea phmArea = phmAreaRepo.findById(req.midwifeDetails.phmAreaId)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid PHM Area ID"));
            midwife.setPhmArea(phmArea);

            midwife.setContactNumber(req.midwifeDetails.contactNumber);
            midwife.setEmail(req.midwifeDetails.email);
//            midwife.setAssignmentDate(req.midwifeDetails.assignmentDate);
            midwife.setQualifications(req.midwifeDetails.qualifications);
            midwifeRepo.save(midwife);
        }

        String token = jwtService.issueToken(user.getUserId(), user.getEmail(), user.getRole().name());

        AuthDtos.AuthResponse resp = new AuthDtos.AuthResponse();
        resp.token = token;
        resp.userId = user.getUserId();
        resp.username = user.getUsername();
        resp.role = user.getRole().name();
        return resp;
    }

    @Transactional(readOnly = true)
    public AuthDtos.AuthResponse login(AuthDtos.LoginRequest req) {
        Optional<User> byEmail = userRepo.findByEmail(req.email); // Optional<User> specifies a object in JSON format
        User user = byEmail.orElseThrow(() -> new IllegalArgumentException("User not found"));

        String suppliedHash = hashService.hashSha256(req.password);
        if (!suppliedHash.equalsIgnoreCase(user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid credentials");
        }
        if (Boolean.FALSE.equals(user.getIsActive())) {
            throw new IllegalStateException("User is inactive");
        }

        String token = jwtService.issueToken(user.getUserId(), user.getEmail(), user.getRole().name());

        AuthDtos.AuthResponse resp = new AuthDtos.AuthResponse();
        resp.token = token;
        resp.userId = user.getUserId();
        resp.username = user.getUsername();
        resp.role = user.getRole().name();

        if (user.getRole() == UserRole.MIDWIFE) {
            midwifeRepo.findByUser_UserId(user.getUserId()).ifPresent(m -> {
                resp.name = m.getName();
                resp.staffId = m.getMidwifeId();
                if (m.getPhmArea() != null) {
                    resp.phmAreaId = m.getPhmArea().getPhmAreaId();
                    resp.phmAreaName = m.getPhmArea().getAreaName();
                }
            });
        } else if (user.getRole() == UserRole.DOCTOR) {
            doctorRepo.findByUser_UserId(user.getUserId()).ifPresent(d -> {
                resp.name = d.getName();
                resp.staffId = d.getDoctorId();
            });
        } else if (user.getRole() == UserRole.MOTHER) {
            motherRepo.findByUser_UserId(user.getUserId()).ifPresent(m -> {
                resp.name = m.getName();
                resp.staffId = m.getMotherId();
            });
        }

        return resp;
    }

    public Iterable<AuthDtos.UserResponse> getAllAdmins() {
        List<User> users = userRepo.findByRole(UserRole.ADMIN);
        return users.stream().map(this::toUserResponse).toList();
    }

    public Iterable<AuthDtos.UserResponse> getAllMothers() {
        List<User> users = userRepo.findByRole(UserRole.MOTHER);
        return users.stream().map(this::toUserResponse).toList();
    }

    public Iterable<AuthDtos.UserResponse> getAllMidwives() {
        List<User> users = userRepo.findByRole(UserRole.MIDWIFE);
        return users.stream().map(this::toUserResponse).toList();
    }

    public Iterable<AuthDtos.UserResponse> getAllDoctors() {
        List<User> users = userRepo.findByRole(UserRole.DOCTOR);
        return users.stream().map(this::toUserResponse).toList();
    }

    public AuthDtos.UserResponse getUserById(Integer userId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));
        return toUserResponse(user);
    }

    @Transactional
    public AuthDtos.UserResponse updateUser(Integer userId, AuthDtos.UserUpdateRequest req) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));

        boolean updated = false;

        if (req.username != null) {
            String newUsername = req.username.trim();
            if (newUsername.isEmpty()) {
                throw new IllegalArgumentException("Username cannot be blank");
            }
            if (!newUsername.equals(user.getUsername())) {
                if (userRepo.existsByUsername(newUsername)) {
                    throw new IllegalArgumentException("Username already taken");
                }
                user.setUsername(newUsername);
                updated = true;
            }
        }

        if (req.email != null) {
            String newEmail = req.email.trim();
            if (newEmail.isEmpty()) {
                throw new IllegalArgumentException("Email cannot be blank");
            }

            Optional<User> existing = userRepo.findByEmail(newEmail);
            if (existing.isPresent() && !existing.get().getUserId().equals(userId)) {
                throw new IllegalArgumentException("Email already registered");
            }

            if (!newEmail.equalsIgnoreCase(user.getEmail())) {
                user.setEmail(newEmail);
                updated = true;
            }
        }

        if (req.password != null) {
            String newPassword = req.password;
            if (newPassword.isBlank()) {
                throw new IllegalArgumentException("Password cannot be blank");
            }
            user.setPasswordHash(hashService.hashSha256(newPassword));
            updated = true;
        }

        if (req.isActive != null && !req.isActive.equals(user.getIsActive())) {
            user.setIsActive(req.isActive);
            updated = true;
        }

        if (!updated) {
            throw new IllegalArgumentException("No fields to update");
        }

        User saved = userRepo.save(user);
        return toUserResponse(saved);
    }

    /**
     * "Delete" implemented as deactivation to avoid FK constraint issues (Doctor/Midwife/etc reference USER).
     */
    @Transactional
    public void deleteUser(Integer userId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));
        user.setIsActive(false);
        userRepo.save(user);
    }

    private AuthDtos.UserResponse toUserResponse(User user) {
        AuthDtos.UserResponse resp = new AuthDtos.UserResponse();
        resp.userId = user.getUserId();
        resp.username = user.getUsername();
        resp.email = user.getEmail();
        resp.role = user.getRole() != null ? user.getRole().name() : null;
        resp.isActive = user.getIsActive();
        resp.createdAt = user.getCreatedAt() != null ? user.getCreatedAt().toString() : null;
        resp.lastLogin = user.getLastLogin() != null ? user.getLastLogin().toString() : null;
        return resp;
    }
}
