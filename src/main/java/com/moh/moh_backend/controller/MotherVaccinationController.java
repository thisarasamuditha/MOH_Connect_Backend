package com.moh.moh_backend.controller;

import com.moh.moh_backend.dto.MotherVaccinationCreateDto;
import com.moh.moh_backend.dto.MotherVaccinationResponseDto;
import com.moh.moh_backend.service.MotherVaccinationService;
import com.moh.moh_backend.config.RequireRoles;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/mother-vaccinations")
public class MotherVaccinationController {

    private final MotherVaccinationService motherVaccinationService;

    public MotherVaccinationController(MotherVaccinationService motherVaccinationService) {
        this.motherVaccinationService = motherVaccinationService;
    }

    @PostMapping
    @RequireRoles({"MIDWIFE", "DOCTOR"})
    public ResponseEntity<?> administerVaccine(@RequestBody MotherVaccinationCreateDto dto, jakarta.servlet.http.HttpServletRequest request) {
        try {
                MotherVaccinationResponseDto response = motherVaccinationService.administerVaccine(dto,
                    (Integer) request.getAttribute("moh.userId"), (String) request.getAttribute("moh.role"));
            return ResponseEntity
                    .created(URI.create("/api/mother-vaccinations/" + response.getVaccinationId()))
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
                MotherVaccinationResponseDto vaccination = motherVaccinationService.getVaccinationById(id,
                    (Integer) request.getAttribute("moh.userId"), (String) request.getAttribute("moh.role"));
            return ResponseEntity.ok(vaccination);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @GetMapping("/by-pregnancy/{pregnancyId}")
    @RequireRoles({"MIDWIFE", "DOCTOR", "MOTHER"})
    public ResponseEntity<?> getVaccinationsByPregnancy(@PathVariable Integer pregnancyId, jakarta.servlet.http.HttpServletRequest request) {
        try {
            List<MotherVaccinationResponseDto> vaccinations = 
                        motherVaccinationService.getVaccinationsByPregnancy(pregnancyId,
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
            List<MotherVaccinationResponseDto> vaccinations = 
                        motherVaccinationService.getVaccinationsByMother(motherId,
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
                motherVaccinationService.deleteVaccination(id,
                    (Integer) request.getAttribute("moh.userId"), (String) request.getAttribute("moh.role"));
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
}
