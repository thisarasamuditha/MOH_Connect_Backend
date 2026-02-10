package com.moh.moh_backend.controller;

import com.moh.moh_backend.model.BabyRecord;
import com.moh.moh_backend.service.BabyRecordService;
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
    public ResponseEntity<?> createBabyRecord(
            @RequestBody BabyRecord babyRecord,
            @RequestParam Integer babyId,
            @RequestParam(required = false) Integer midwifeId,
            @RequestParam(required = false) Integer doctorId) {
        try {
            BabyRecord created = babyRecordService.createBabyRecord(
                    babyRecord, babyId, midwifeId, doctorId);
            return ResponseEntity
                    .created(URI.create("/api/baby-records/" + created.getRecordId()))
                    .body(created);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getBabyRecordById(@PathVariable Integer id) {
        try {
            BabyRecord record = babyRecordService.getBabyRecordById(id);
            return ResponseEntity.ok(record);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @GetMapping("/by-baby/{babyId}")
    public ResponseEntity<?> getBabyRecordsByBabyId(@PathVariable Integer babyId) {
        try {
            List<BabyRecord> records = babyRecordService.getBabyRecordsByBabyId(babyId);
            return ResponseEntity.ok(records);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateBabyRecord(
            @PathVariable Integer id,
            @RequestBody BabyRecord babyRecord,
            @RequestParam(required = false) Integer midwifeId,
            @RequestParam(required = false) Integer doctorId) {
        try {
            BabyRecord updated = babyRecordService.updateBabyRecord(
                    id, babyRecord, midwifeId, doctorId);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteBabyRecord(@PathVariable Integer id) {
        try {
            babyRecordService.deleteBabyRecord(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
}
