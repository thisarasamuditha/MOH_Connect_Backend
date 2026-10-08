package com.moh.moh_backend.controller;

import com.moh.moh_backend.dto.MotherResponse;
import com.moh.moh_backend.dto.PregnancyResponse;
import com.moh.moh_backend.model.Pregnancy;
import com.moh.moh_backend.service.PregnancyService;
import com.moh.moh_backend.util.JwtService;
import com.moh.moh_backend.config.RequireRoles;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/pregnancies")
public class PregnancyController {

    private final PregnancyService pregnancyService;
    private final JwtService jwtService;

    public PregnancyController(PregnancyService pregnancyService, JwtService jwtService) {
        this.pregnancyService = pregnancyService;
        this.jwtService = jwtService;
    }

    @PostMapping
    @RequireRoles({"MIDWIFE", "DOCTOR"})
    public ResponseEntity<?> createPregnancy(
            @RequestHeader("Authorization") String authorization,
            @RequestParam Integer motherId,
            @RequestBody Pregnancy pregnancy,
            jakarta.servlet.http.HttpServletRequest request) {
        
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body("Missing Bearer token");
        }
        String token = authorization.substring("Bearer ".length()).trim();

        String role = jwtService.getRole(token);
        if (!"MIDWIFE".equalsIgnoreCase(role) && !"DOCTOR".equalsIgnoreCase(role)) {
            return ResponseEntity.status(403).body("Only midwives and doctors can create pregnancies");
        }

        try {
                Pregnancy created = pregnancyService.createPregnancy(pregnancy, motherId,
                    (Integer) request.getAttribute("moh.userId"), role);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping("/{pregnancyId}")
    @RequireRoles({"MIDWIFE", "DOCTOR", "MOTHER"})
    public ResponseEntity<?> getPregnancyById(
            @RequestHeader("Authorization") String authorization,
            @PathVariable Integer pregnancyId,
            jakarta.servlet.http.HttpServletRequest request) {
        
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body("Missing Bearer token");
        }

        try {
                Pregnancy pregnancy = pregnancyService.getPregnancyById(pregnancyId,
                    (Integer) request.getAttribute("moh.userId"),
                    (String) request.getAttribute("moh.role"));
            return ResponseEntity.ok(pregnancy);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @GetMapping("/mother/{motherId}")
    @RequireRoles({"MIDWIFE", "DOCTOR", "MOTHER"})
    public ResponseEntity<?> getPregnanciesByMotherId(
            @RequestHeader("Authorization") String authorization,
            @PathVariable Integer motherId,
            jakarta.servlet.http.HttpServletRequest request) {
        
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body("Missing Bearer token");
        }

        try {
                List<Pregnancy> pregnancies = pregnancyService.getPregnanciesByMotherId(motherId,
                    (Integer) request.getAttribute("moh.userId"),
                    (String) request.getAttribute("moh.role"));
            return ResponseEntity.ok(pregnancies);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }


    // Get mothers by PHM area ID (Doctor, Midwife, Admin)
    @GetMapping("/by-phm-area/{phmAreaId}")
    @RequireRoles({"DOCTOR", "MIDWIFE", "ADMIN"})
    public ResponseEntity<?> getPregnancyByPhmArea(
            @PathVariable Integer phmAreaId,
            @RequestHeader(value = "Authorization", required = false) String authorization) {


        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body(Map.of("error", "Missing Bearer token"));
        }
        String token = authorization.substring("Bearer ".length()).trim();


        String role = jwtService.getRole(token);
        List<String> allowedRoles = List.of("DOCTOR", "MIDWIFE", "ADMIN");

        if (role == null || allowedRoles.stream().noneMatch(r -> r.equalsIgnoreCase(role))) {
            return ResponseEntity.status(403).body(Map.of("error", "Access denied: Only DOCTOR, MIDWIFE, or ADMIN can access this"));
        }


        try {
            List<PregnancyResponse> response = pregnancyService.getPregnancyByPhmArea(phmAreaId);
            return ResponseEntity.ok(response);
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
            return ResponseEntity.status(500).body(Map.of(
                    "error", "Failed to fetch mothers: " + e.getMessage(),
                    "details", e.getClass().getName()
            ));
        }
    }


    @GetMapping("/active")
    @RequireRoles({"MIDWIFE", "DOCTOR"})
    public ResponseEntity<?> getActivePregnancies(
            @RequestHeader("Authorization") String authorization,
            jakarta.servlet.http.HttpServletRequest request) {
        
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body("Missing Bearer token");
        }

        String token = authorization.substring("Bearer ".length()).trim();
        String role = jwtService.getRole(token);
        
        if (!"MIDWIFE".equalsIgnoreCase(role) && !"DOCTOR".equalsIgnoreCase(role)) {
            return ResponseEntity.status(403).body("Only midwives and doctors can view all active pregnancies");
        }

        try {
            Integer userId = jwtService.getUserId(token);

            List<Pregnancy> pregnancies = pregnancyService.getActivePregnancies(
                    userId,role);
            return ResponseEntity.ok(pregnancies);
        }  catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PutMapping("/{pregnancyId}")
    @RequireRoles({"MIDWIFE", "DOCTOR"})
    public ResponseEntity<?> updatePregnancy(
            @RequestHeader("Authorization") String authorization,
            @PathVariable Integer pregnancyId,
            @RequestBody Pregnancy pregnancy,
            jakarta.servlet.http.HttpServletRequest request) {
        
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body("Missing Bearer token");
        }
        
        String token = authorization.substring("Bearer ".length()).trim();
        String role = jwtService.getRole(token);
        Integer userId = jwtService.getUserId(token);
        
        if (!"MIDWIFE".equalsIgnoreCase(role) && !"DOCTOR".equalsIgnoreCase(role)) {
            return ResponseEntity.status(403).body("Only midwives and doctors can update pregnancies");
        }

        try {
                Pregnancy updated = pregnancyService.updatePregnancy(pregnancyId, pregnancy,
                        userId,
                        role);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @DeleteMapping("/{pregnancyId}")
    @RequireRoles("DOCTOR")
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
