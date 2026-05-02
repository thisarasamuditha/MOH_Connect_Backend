package com.moh.moh_backend.controller;

import com.moh.moh_backend.dto.FamilyResponse;
import com.moh.moh_backend.dto.MotherRegisterRequest;
import com.moh.moh_backend.dto.MotherResponse;
import com.moh.moh_backend.dto.PregnancyResponse;
import com.moh.moh_backend.service.MotherService;
import com.moh.moh_backend.util.JwtService;
import com.moh.moh_backend.model.Mother;
import com.moh.moh_backend.model.Pregnancy;
import com.moh.moh_backend.model.User;
import com.moh.moh_backend.repository.MotherRepository;
import com.moh.moh_backend.repository.PregnancyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/mothers")
public class MotherController {
    private final MotherService motherService;
    private final JwtService jwtService;
    @Autowired
    private MotherRepository motherRepository;
    @Autowired
    private PregnancyRepository pregnancyRepository;

    public MotherController(MotherService motherService, JwtService jwtService) {
        this.motherService = motherService;
        this.jwtService = jwtService;
    }
    // New endpoint: Get mother details (with email) and active pregnancy by userId
    @GetMapping("/me")
    public ResponseEntity<?> getMotherDetailsAndActivePregnancy(@RequestHeader("Authorization") String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body(Map.of("error", "Missing Bearer token"));
        }
        String token = authorization.substring("Bearer ".length()).trim();
        Integer userId = jwtService.getUserId(token);
        // Find mother by userId
        Mother mother = motherRepository.findByUser_UserId(userId)
                .orElse(null);
        if (mother == null) {
            return ResponseEntity.status(404).body(Map.of("error", "Mother not found for user"));
        }
        // Get email from user
        User user = mother.getUser();
        String email = user != null ? user.getEmail() : null;
        // Get active pregnancy for this mother
        Pregnancy activePregnancy = pregnancyRepository
                .findByMother_MotherIdAndPregnancyStatus(mother.getMotherId(), Pregnancy.PregnancyStatus.ACTIVE)
                .stream().findFirst().orElse(null);
        
        java.util.Map<String, Object> response = new java.util.HashMap<>();
        response.put("mother", MotherResponse.from(mother));
        response.put("email", email != null ? email : "");
        response.put("activePregnancy", activePregnancy != null ? PregnancyResponse.from(activePregnancy) : null);
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerMother(
            @RequestHeader("Authorization") String authorization,
            @RequestBody MotherRegisterRequest req) {

        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body(Map.of("error", "Missing Bearer token"));
        }
        String token = authorization.substring("Bearer ".length()).trim();

        String role = jwtService.getRole(token);
        if (!"MIDWIFE".equalsIgnoreCase(role)) {
            return ResponseEntity.status(403).body(Map.of("error", "Only midwives can register mothers"));
        }

        try {
            Integer midwifeUserId = jwtService.getUserId(token);
            motherService.registerMother(req, midwifeUserId);
            return ResponseEntity.ok(Map.of("message", "Mother registered"));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Registration failed: " + e.getMessage()));
        }
    }

    @GetMapping("/my-mothers")
    public ResponseEntity<?> getMyMothers(
            @RequestHeader("Authorization") String authorization) {

        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body(Map.of("error", "Missing Bearer token"));
        }
        String token = authorization.substring("Bearer ".length()).trim();

        String role = jwtService.getRole(token);
        if (!"MIDWIFE".equalsIgnoreCase(role)) {
            return ResponseEntity.status(403).body(Map.of("error", "Only midwives can access this"));
        }

        try {
            Integer midwifeUserId = jwtService.getUserId(token);
            List<MotherResponse> mothers = motherService.getMothersByMidwife(midwifeUserId);
            return ResponseEntity.ok(mothers);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Failed to fetch mothers: " + e.getMessage()));
        }
    }

    @GetMapping("/my-families")
    public ResponseEntity<?> getMyFamilies(
            @RequestHeader("Authorization") String authorization) {

        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body(Map.of("error", "Missing Bearer token"));
        }
        String token = authorization.substring("Bearer ".length()).trim();

        String role = jwtService.getRole(token);
        if (!"MIDWIFE".equalsIgnoreCase(role)) {
            return ResponseEntity.status(403).body(Map.of("error", "Only midwives can access this"));
        }

        try {
            Integer midwifeUserId = jwtService.getUserId(token);
            List<FamilyResponse> families = motherService.getFamiliesForMidwife(midwifeUserId);
            return ResponseEntity.ok(families);
        } catch (IllegalArgumentException e) {
            System.err.println("Bad request error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            System.err.println("State error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(409).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", "Failed to fetch families: " + e.getMessage(), "details", e.getClass().getName()));
        }
    }
}