package com.moh.moh_backend.controller;

import com.moh.moh_backend.dto.MotherVaccinationCreateDto;
import com.moh.moh_backend.dto.MotherVaccinationResponseDto;
import com.moh.moh_backend.service.MotherVaccinationService;
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
    public ResponseEntity<?> administerVaccine(@RequestBody MotherVaccinationCreateDto dto) {
        try {
            MotherVaccinationResponseDto response = motherVaccinationService.administerVaccine(dto);
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
    public ResponseEntity<?> getVaccinationById(@PathVariable Integer id) {
        try {
            MotherVaccinationResponseDto vaccination = motherVaccinationService.getVaccinationById(id);
            return ResponseEntity.ok(vaccination);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @GetMapping("/by-pregnancy/{pregnancyId}")
    public ResponseEntity<?> getVaccinationsByPregnancy(@PathVariable Integer pregnancyId) {
        try {
            List<MotherVaccinationResponseDto> vaccinations = 
                    motherVaccinationService.getVaccinationsByPregnancy(pregnancyId);
            return ResponseEntity.ok(vaccinations);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping("/by-mother/{motherId}")
    public ResponseEntity<?> getVaccinationsByMother(@PathVariable Integer motherId) {
        try {
            List<MotherVaccinationResponseDto> vaccinations = 
                    motherVaccinationService.getVaccinationsByMother(motherId);
            return ResponseEntity.ok(vaccinations);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteVaccination(@PathVariable Integer id) {
        try {
            motherVaccinationService.deleteVaccination(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
}
