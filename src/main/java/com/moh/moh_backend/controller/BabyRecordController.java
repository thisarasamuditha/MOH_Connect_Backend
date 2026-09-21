package com.moh.moh_backend.controller;

import com.moh.moh_backend.model.BabyRecord;
import com.moh.moh_backend.service.BabyRecordService;
import com.moh.moh_backend.config.RequireRoles;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/baby-records")
public class BabyRecordController {

    private final BabyRecordService babyRecordService;

    public BabyRecordController(BabyRecordService babyRecordService) {
        this.babyRecordService = babyRecordService;
    }

    @PostMapping
    @RequireRoles({"MIDWIFE", "DOCTOR"})
    public ResponseEntity<?> createBabyRecord(
            @RequestBody BabyRecord babyRecord,
            @RequestParam Integer babyId,
            @RequestParam(required = false) Integer midwifeId,
            @RequestParam(required = false) Integer doctorId,
            jakarta.servlet.http.HttpServletRequest request) {
        try {
            BabyRecord created = babyRecordService.createBabyRecord(
                    babyRecord, babyId, midwifeId, doctorId,
                    (Integer) request.getAttribute("moh.userId"),
                    (String) request.getAttribute("moh.role"));
            return ResponseEntity
                    .created(URI.create("/api/baby-records/" + created.getRecordId()))
                    .body(created);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping("/{id}")
    @RequireRoles({"MIDWIFE", "DOCTOR", "MOTHER"})
    public ResponseEntity<?> getBabyRecordById(@PathVariable Integer id,
                                                jakarta.servlet.http.HttpServletRequest request) {
        try {
                BabyRecord record = babyRecordService.getBabyRecordById(id,
                    (Integer) request.getAttribute("moh.userId"),
                    (String) request.getAttribute("moh.role"));
            return ResponseEntity.ok(record);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @GetMapping("/by-baby/{babyId}")
    @RequireRoles({"MIDWIFE", "DOCTOR", "MOTHER"})
    public ResponseEntity<?> getBabyRecordsByBabyId(@PathVariable Integer babyId,
                                                     jakarta.servlet.http.HttpServletRequest request) {
        try {
                List<BabyRecord> records = babyRecordService.getBabyRecordsByBabyId(babyId,
                    (Integer) request.getAttribute("moh.userId"),
                    (String) request.getAttribute("moh.role"));
            return ResponseEntity.ok(records);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @RequireRoles({"MIDWIFE", "DOCTOR"})
    public ResponseEntity<?> updateBabyRecord(
            @PathVariable Integer id,
            @RequestBody BabyRecord babyRecord,
            @RequestParam(required = false) Integer midwifeId,
            @RequestParam(required = false) Integer doctorId,
            jakarta.servlet.http.HttpServletRequest request) {
        try {
            BabyRecord updated = babyRecordService.updateBabyRecord(
                    id, babyRecord, midwifeId, doctorId,
                    (Integer) request.getAttribute("moh.userId"),
                    (String) request.getAttribute("moh.role"));
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @RequireRoles("DOCTOR")
    public ResponseEntity<?> deleteBabyRecord(@PathVariable Integer id,
                                               jakarta.servlet.http.HttpServletRequest request) {
        try {
                babyRecordService.deleteBabyRecord(id,
                    (Integer) request.getAttribute("moh.userId"),
                    (String) request.getAttribute("moh.role"));
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
}
