package com.moh.moh_backend.controller;

import com.moh.moh_backend.dto.BabyVaccinationCreateDto;
import com.moh.moh_backend.dto.BabyVaccinationResponseDto;
import com.moh.moh_backend.service.BabyVaccinationService;
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
    public ResponseEntity<?> administerVaccine(@RequestBody BabyVaccinationCreateDto dto) {
        try {
            BabyVaccinationResponseDto response = babyVaccinationService.administerVaccine(dto);
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
    public ResponseEntity<?> getVaccinationById(@PathVariable Integer id) {
        try {
            BabyVaccinationResponseDto vaccination = babyVaccinationService.getVaccinationById(id);
            return ResponseEntity.ok(vaccination);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @GetMapping("/by-baby/{babyId}")
    public ResponseEntity<?> getVaccinationsByBaby(@PathVariable Integer babyId) {
        try {
            List<BabyVaccinationResponseDto> vaccinations = 
                    babyVaccinationService.getVaccinationsByBaby(babyId);
            return ResponseEntity.ok(vaccinations);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping("/by-mother/{motherId}")
    public ResponseEntity<?> getVaccinationsByMother(@PathVariable Integer motherId) {
        try {
            List<BabyVaccinationResponseDto> vaccinations = 
                    babyVaccinationService.getVaccinationsByMother(motherId);
            return ResponseEntity.ok(vaccinations);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteVaccination(@PathVariable Integer id) {
        try {
            babyVaccinationService.deleteVaccination(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
}
