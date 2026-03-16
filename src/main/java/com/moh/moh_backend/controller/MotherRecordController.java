package com.moh.moh_backend.controller;

import com.moh.moh_backend.model.MotherRecord;
import com.moh.moh_backend.repository.DoctorRepository;
import com.moh.moh_backend.repository.MidwifeRepository;
import com.moh.moh_backend.service.MotherRecordService;
import com.moh.moh_backend.util.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/mother-records")
public class MotherRecordController {

    private final MotherRecordService motherRecordService;
    private final JwtService jwtService;
    private final MidwifeRepository midwifeRepository;
    private final DoctorRepository doctorRepository;

    public MotherRecordController(MotherRecordService motherRecordService,
                                  JwtService jwtService,
                                  MidwifeRepository midwifeRepository,
                                  DoctorRepository doctorRepository) {
        this.motherRecordService = motherRecordService;
        this.jwtService = jwtService;
        this.midwifeRepository = midwifeRepository;
        this.doctorRepository = doctorRepository;
    }

    @PostMapping
    public ResponseEntity<?> createMotherRecord(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody MotherRecord motherRecord,
            @RequestParam Integer pregnancyId,
            @RequestParam(required = false) Integer midwifeId,
            @RequestParam(required = false) Integer doctorId) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Missing Bearer token"));
        }

        String token = authorization.substring("Bearer ".length()).trim();
        String role = jwtService.getRole(token);
        Integer userId = jwtService.getUserId(token);

        if (role == null || userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid token"));
        }

        if (!"MIDWIFE".equalsIgnoreCase(role) && !"DOCTOR".equalsIgnoreCase(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Only midwives and doctors can add visit records"));
        }

        if ("MIDWIFE".equalsIgnoreCase(role) && midwifeId == null) {
            midwifeId = midwifeRepository.findByUser_UserId(userId)
                    .map(m -> m.getMidwifeId())
                    .orElse(null);
        }
        if ("DOCTOR".equalsIgnoreCase(role) && doctorId == null) {
            doctorId = doctorRepository.findByUser_UserId(userId)
                    .map(d -> d.getDoctorId())
                    .orElse(null);
        }

        try {
            MotherRecord created = motherRecordService.createMotherRecord(
                    motherRecord, pregnancyId, midwifeId, doctorId);
            return ResponseEntity
                    .created(URI.create("/api/mother-records/" + created.getRecordId()))
                    .body(created);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getMotherRecordById(@PathVariable Integer id) {
        try {
            MotherRecord record = motherRecordService.getMotherRecordById(id);
            return ResponseEntity.ok(record);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @GetMapping("/by-pregnancy/{pregnancyId}")
    public ResponseEntity<?> getMotherRecordsByPregnancyId(@PathVariable Integer pregnancyId) {
        try {
            List<MotherRecord> records = motherRecordService.getMotherRecordsByPregnancyId(pregnancyId);
            return ResponseEntity.ok(records);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateMotherRecord(
            @PathVariable Integer id,
            @RequestBody MotherRecord motherRecord,
            @RequestParam(required = false) Integer midwifeId,
            @RequestParam(required = false) Integer doctorId) {
        try {
            MotherRecord updated = motherRecordService.updateMotherRecord(
                    id, motherRecord, midwifeId, doctorId);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteMotherRecord(@PathVariable Integer id) {
        try {
            motherRecordService.deleteMotherRecord(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
}
