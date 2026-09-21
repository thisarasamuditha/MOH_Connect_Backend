package com.moh.moh_backend.controller;

import com.moh.moh_backend.dto.BabyVaccinationCreateDto;
import com.moh.moh_backend.dto.BabyVaccinationResponseDto;
import com.moh.moh_backend.service.BabyVaccinationService;
import com.moh.moh_backend.config.RequireRoles;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/baby-vaccinations")
public class BabyVaccinationController {

    private final BabyVaccinationService babyVaccinationService;

    public BabyVaccinationController(BabyVaccinationService babyVaccinationService) {
        this.babyVaccinationService = babyVaccinationService;
    }

    @PostMapping
    @RequireRoles({"MIDWIFE", "DOCTOR"})
    public ResponseEntity<?> administerVaccine(@RequestBody BabyVaccinationCreateDto dto, jakarta.servlet.http.HttpServletRequest request) {
        try {
                BabyVaccinationResponseDto response = babyVaccinationService.administerVaccine(dto,
                    (Integer) request.getAttribute("moh.userId"), (String) request.getAttribute("moh.role"));
            return ResponseEntity
                    .created(URI.create("/api/baby-vaccinations/" + response.getVaccinationId()))
                    .body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping("/{id}")
    @RequireRoles({"MIDWIFE", "DOCTOR", "MOTHER"})
    public ResponseEntity<?> getVaccinationById(@PathVariable Integer id, jakarta.servlet.http.HttpServletRequest request) {
        try {
                BabyVaccinationResponseDto vaccination = babyVaccinationService.getVaccinationById(id,
                    (Integer) request.getAttribute("moh.userId"), (String) request.getAttribute("moh.role"));
            return ResponseEntity.ok(vaccination);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @GetMapping("/by-baby/{babyId}")
    @RequireRoles({"MIDWIFE", "DOCTOR", "MOTHER"})
    public ResponseEntity<?> getVaccinationsByBaby(@PathVariable Integer babyId, jakarta.servlet.http.HttpServletRequest request) {
        try {
            List<BabyVaccinationResponseDto> vaccinations = 
                        babyVaccinationService.getVaccinationsByBaby(babyId,
                            (Integer) request.getAttribute("moh.userId"), (String) request.getAttribute("moh.role"));
            return ResponseEntity.ok(vaccinations);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping("/by-mother/{motherId}")
    @RequireRoles({"MIDWIFE", "DOCTOR", "MOTHER"})
    public ResponseEntity<?> getVaccinationsByMother(@PathVariable Integer motherId, jakarta.servlet.http.HttpServletRequest request) {
        try {
            List<BabyVaccinationResponseDto> vaccinations = 
                        babyVaccinationService.getVaccinationsByMother(motherId,
                            (Integer) request.getAttribute("moh.userId"), (String) request.getAttribute("moh.role"));
            return ResponseEntity.ok(vaccinations);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @RequireRoles("DOCTOR")
    public ResponseEntity<?> deleteVaccination(@PathVariable Integer id, jakarta.servlet.http.HttpServletRequest request) {
        try {
                babyVaccinationService.deleteVaccination(id,
                    (Integer) request.getAttribute("moh.userId"), (String) request.getAttribute("moh.role"));
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
}
