package com.moh.moh_backend.controller;

import com.moh.moh_backend.model.MotherRecord;
import com.moh.moh_backend.service.MotherRecordService;
import com.moh.moh_backend.config.RequireRoles;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/mother-records")
public class MotherRecordController {

    private final MotherRecordService motherRecordService;

    public MotherRecordController(MotherRecordService motherRecordService) {
        this.motherRecordService = motherRecordService;
    }

    @PostMapping
    @RequireRoles({"MIDWIFE", "DOCTOR"})
    public ResponseEntity<?> createMotherRecord(
            @RequestBody MotherRecord motherRecord,
            @RequestParam Integer pregnancyId,
            @RequestParam(required = false) Integer midwifeId,
            @RequestParam(required = false) Integer doctorId,
            jakarta.servlet.http.HttpServletRequest request) {
        try {
            MotherRecord created = motherRecordService.createMotherRecord(
                    motherRecord, pregnancyId, midwifeId, doctorId,
                    (Integer) request.getAttribute("moh.userId"),
                    (String) request.getAttribute("moh.role"));
            return ResponseEntity
                    .created(URI.create("/api/mother-records/" + created.getRecordId()))
                    .body(created);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping("/{id}")
    @RequireRoles({"MIDWIFE", "DOCTOR", "MOTHER"})
    public ResponseEntity<?> getMotherRecordById(@PathVariable Integer id,
                                                  jakarta.servlet.http.HttpServletRequest request) {
        try {
                MotherRecord record = motherRecordService.getMotherRecordById(id,
                    (Integer) request.getAttribute("moh.userId"),
                    (String) request.getAttribute("moh.role"));
            return ResponseEntity.ok(record);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @GetMapping("/by-pregnancy/{pregnancyId}")
    @RequireRoles({"MIDWIFE", "DOCTOR", "MOTHER"})
    public ResponseEntity<?> getMotherRecordsByPregnancyId(@PathVariable Integer pregnancyId,
                                                            jakarta.servlet.http.HttpServletRequest request) {
        try {
                List<MotherRecord> records = motherRecordService.getMotherRecordsByPregnancyId(pregnancyId,
                    (Integer) request.getAttribute("moh.userId"),
                    (String) request.getAttribute("moh.role"));
            return ResponseEntity.ok(records);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @RequireRoles({"MIDWIFE", "DOCTOR"})
    public ResponseEntity<?> updateMotherRecord(
            @PathVariable Integer id,
            @RequestBody MotherRecord motherRecord,
            @RequestParam(required = false) Integer midwifeId,
            @RequestParam(required = false) Integer doctorId,
            jakarta.servlet.http.HttpServletRequest request) {
        try {
            MotherRecord updated = motherRecordService.updateMotherRecord(
                    id, motherRecord, midwifeId, doctorId,
                    (Integer) request.getAttribute("moh.userId"),
                    (String) request.getAttribute("moh.role"));
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PutMapping("/{id}/review")
    @RequireRoles("DOCTOR")
    public ResponseEntity<?> reviewMotherRecord(
            @PathVariable Integer id,
            @RequestParam String status,
            @RequestParam(required = false) String comment,
            jakarta.servlet.http.HttpServletRequest request) {
        try {
            MotherRecord reviewed = motherRecordService.reviewMotherRecord(
                    id, status, comment,
                    (Integer) request.getAttribute("moh.userId"),
                    (String) request.getAttribute("moh.role"));
            return ResponseEntity.ok(reviewed);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @RequireRoles("DOCTOR")
    public ResponseEntity<?> deleteMotherRecord(@PathVariable Integer id,
                                                 jakarta.servlet.http.HttpServletRequest request) {
        try {
                motherRecordService.deleteMotherRecord(id,
                    (Integer) request.getAttribute("moh.userId"),
                    (String) request.getAttribute("moh.role"));
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
}
