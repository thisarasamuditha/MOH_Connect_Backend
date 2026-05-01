package com.moh.moh_backend.controller;

import com.moh.moh_backend.model.Pregnancy;
import com.moh.moh_backend.service.PregnancyService;
import com.moh.moh_backend.util.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pregnancies")
public class PregnancyController {

    private final PregnancyService pregnancyService;
    private final JwtService jwtService;

    public PregnancyController(PregnancyService pregnancyService, JwtService jwtService) {
        this.pregnancyService = pregnancyService;
        this.jwtService = jwtService;
    }

    @PostMapping
    public ResponseEntity<?> createPregnancy(
            @RequestHeader("Authorization") String authorization,
            @RequestParam Integer motherId,
            @RequestBody Pregnancy pregnancy) {
        
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body("Missing Bearer token");
        }
        String token = authorization.substring("Bearer ".length()).trim();

        String role = jwtService.getRole(token);
        if (!"MIDWIFE".equalsIgnoreCase(role) && !"DOCTOR".equalsIgnoreCase(role)) {
            return ResponseEntity.status(403).body("Only midwives and doctors can create pregnancies");
        }

        try {
            Pregnancy created = pregnancyService.createPregnancy(pregnancy, motherId);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping("/{pregnancyId}")
    public ResponseEntity<?> getPregnancyById(
            @RequestHeader("Authorization") String authorization,
            @PathVariable Integer pregnancyId) {
        
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body("Missing Bearer token");
        }

        try {
            Pregnancy pregnancy = pregnancyService.getPregnancyById(pregnancyId);
            return ResponseEntity.ok(pregnancy);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @GetMapping("/mother/{motherId}")
    public ResponseEntity<?> getPregnanciesByMotherId(
            @RequestHeader("Authorization") String authorization,
            @PathVariable Integer motherId) {
        
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body("Missing Bearer token");
        }

        try {
            List<Pregnancy> pregnancies = pregnancyService.getPregnanciesByMotherId(motherId);
            return ResponseEntity.ok(pregnancies);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping("/active")
    public ResponseEntity<?> getActivePregnancies(
            @RequestHeader("Authorization") String authorization) {
        
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body("Missing Bearer token");
        }

        String token = authorization.substring("Bearer ".length()).trim();
        String role = jwtService.getRole(token);
        
        if (!"MIDWIFE".equalsIgnoreCase(role) && !"DOCTOR".equalsIgnoreCase(role)) {
            return ResponseEntity.status(403).body("Only midwives and doctors can view all active pregnancies");
        }

        try {
            List<Pregnancy> pregnancies = pregnancyService.getActivePregnancies();
            return ResponseEntity.ok(pregnancies);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PutMapping("/{pregnancyId}")
    public ResponseEntity<?> updatePregnancy(
            @RequestHeader("Authorization") String authorization,
            @PathVariable Integer pregnancyId,
            @RequestBody Pregnancy pregnancy) {
        
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body("Missing Bearer token");
        }
        
        String token = authorization.substring("Bearer ".length()).trim();
        String role = jwtService.getRole(token);
        
        if (!"MIDWIFE".equalsIgnoreCase(role) && !"DOCTOR".equalsIgnoreCase(role)) {
            return ResponseEntity.status(403).body("Only midwives and doctors can update pregnancies");
        }

        try {
            Pregnancy updated = pregnancyService.updatePregnancy(pregnancyId, pregnancy);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @DeleteMapping("/{pregnancyId}")
    public ResponseEntity<?> deletePregnancy(
            @RequestHeader("Authorization") String authorization,
            @PathVariable Integer pregnancyId) {
        
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body("Missing Bearer token");
        }
        
        String token = authorization.substring("Bearer ".length()).trim();
        String role = jwtService.getRole(token);
        
        if (!"DOCTOR".equalsIgnoreCase(role)) {
            return ResponseEntity.status(403).body("Only doctors can delete pregnancies");
        }

        try {
            pregnancyService.deletePregnancy(pregnancyId);
            return ResponseEntity.ok("Pregnancy deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
