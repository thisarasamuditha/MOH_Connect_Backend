package com.moh.moh_backend.controller;

import com.moh.moh_backend.model.MotherRecord;
import com.moh.moh_backend.service.MotherRecordService;
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
    public ResponseEntity<?> createMotherRecord(
            @RequestBody MotherRecord motherRecord,
            @RequestParam Integer pregnancyId,
            @RequestParam(required = false) Integer midwifeId,
            @RequestParam(required = false) Integer doctorId) {
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
