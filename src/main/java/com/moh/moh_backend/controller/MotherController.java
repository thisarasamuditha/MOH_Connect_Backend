package com.moh.moh_backend.controller;

import com.moh.moh_backend.dto.FamilyResponse;
import com.moh.moh_backend.dto.MotherRegisterRequest;
import com.moh.moh_backend.dto.MotherResponse;
import com.moh.moh_backend.service.MotherService;
import com.moh.moh_backend.util.JwtService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mothers")
public class MotherController {
    private final MotherService motherService;
    private final JwtService jwtService;

    public MotherController(MotherService motherService, JwtService jwtService) {
        this.motherService = motherService;
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerMother(
            @RequestHeader("Authorization") String authorization,
            @RequestBody MotherRegisterRequest req) {

        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body("Missing Bearer token");
        }
        String token = authorization.substring("Bearer ".length()).trim();

        // Enforce role MIDWIFE
        String role = jwtService.getRole(token);
        if (!"MIDWIFE".equalsIgnoreCase(role)) {
            return ResponseEntity.status(403).body("Only midwives can register mothers");
        }
        Integer midwifeUserId = jwtService.getUserId(token);

        motherService.registerMother(req, midwifeUserId);
        return ResponseEntity.ok("Mother registered");
    }

    @GetMapping("/my-mothers")
    public ResponseEntity<?> getMyMothers(
            @RequestHeader("Authorization") String authorization) {

        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body("Missing Bearer token");
        }
        String token = authorization.substring("Bearer ".length()).trim();

        String role = jwtService.getRole(token);
        if (!"MIDWIFE".equalsIgnoreCase(role)) {
            return ResponseEntity.status(403).body("Only midwives can access this");
        }
        Integer midwifeUserId = jwtService.getUserId(token);

        List<MotherResponse> mothers = motherService.getMothersByMidwife(midwifeUserId);
        return ResponseEntity.ok(mothers);
    }

    @GetMapping("/my-families")
    public ResponseEntity<?> getMyFamilies(
            @RequestHeader("Authorization") String authorization) {

        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body("Missing Bearer token");
        }
        String token = authorization.substring("Bearer ".length()).trim();

        String role = jwtService.getRole(token);
        if (!"MIDWIFE".equalsIgnoreCase(role)) {
            return ResponseEntity.status(403).body("Only midwives can access this");
        }
        Integer midwifeUserId = jwtService.getUserId(token);

        List<FamilyResponse> families = motherService.getFamiliesForMidwife(midwifeUserId);
        return ResponseEntity.ok(families);
    }
}