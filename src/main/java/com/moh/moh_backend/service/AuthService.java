package com.moh.moh_backend.service;

import com.moh.moh_backend.dto.AuthDtos;
import com.moh.moh_backend.model.*;
import com.moh.moh_backend.repository.DoctorRepository;
import com.moh.moh_backend.repository.MidwifeRepository;
import com.moh.moh_backend.repository.PhmAreaRepository;
import com.moh.moh_backend.repository.UserRepository;
import com.moh.moh_backend.util.JwtService;
import com.moh.moh_backend.util.PasswordHashService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final PhmAreaRepository phmAreaRepo;

    public AuthService(UserRepository userRepo, PasswordHashService hashService, JwtService jwtService, DoctorRepository doctorRepo, MidwifeRepository midwifeRepo,PhmAreaRepository phmAreaRepo) {
        this.userRepo = userRepo;
        this.hashService = hashService;
        this.jwtService = jwtService;
        this.doctorRepo = doctorRepo;
        this.midwifeRepo = midwifeRepo;
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
        resp.username = user.getUsername();
        resp.role = user.getRole().name();
        return resp;
    }

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
        resp.username = user.getUsername();
        resp.role = user.getRole().name();
        return resp;
    }
}
