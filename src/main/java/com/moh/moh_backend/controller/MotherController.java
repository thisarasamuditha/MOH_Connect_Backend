package com.moh.moh_backend.controller;

import com.moh.moh_backend.dto.FamilyResponse;
import com.moh.moh_backend.dto.MotherRegisterRequest;
import com.moh.moh_backend.dto.MotherResponse;
import com.moh.moh_backend.service.MotherService;
import com.moh.moh_backend.util.JwtService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Failed to fetch families: " + e.getMessage()));
        }
    }
}