package com.moh.moh_backend.controller;

import com.moh.moh_backend.dto.SectionDtos.SectionCreateRequest;
import com.moh.moh_backend.dto.SectionDtos.SectionResponse;
import com.moh.moh_backend.dto.SectionDtos.SectionUpdateRequest;
import com.moh.moh_backend.service.SectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sections")
@RequiredArgsConstructor
public class SectionController {

    private final SectionService sectionService;

    @PostMapping
    public ResponseEntity<SectionResponse> create(@RequestBody SectionCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(sectionService.create(request));
    }

    @GetMapping("/{sectionId}")
    public ResponseEntity<SectionResponse> getById(@PathVariable Integer sectionId) {
        return ResponseEntity.ok(sectionService.getById(sectionId));
    }

    @GetMapping("/midwife/{midwifeId}")
    public ResponseEntity<List<SectionResponse>> getByMidwife(@PathVariable Integer midwifeId) {
        return ResponseEntity.ok(sectionService.getByMidwifeId(midwifeId));
    }

    @GetMapping("/midwife/{midwifeId}/active")
    public ResponseEntity<List<SectionResponse>> getActiveSectionsByMidwife(@PathVariable Integer midwifeId) {
        return ResponseEntity.ok(sectionService.getActiveSectionsByMidwifeId(midwifeId));
    }

    @GetMapping("/phm-area/{phmAreaId}")
    public ResponseEntity<List<SectionResponse>> getByPhmArea(@PathVariable Integer phmAreaId) {
        return ResponseEntity.ok(sectionService.getByPhmAreaId(phmAreaId));
    }

    @GetMapping("/phm-area/{phmAreaId}/active")
    public ResponseEntity<List<SectionResponse>> getActiveSectionsByPhmArea(@PathVariable Integer phmAreaId) {
        return ResponseEntity.ok(sectionService.getActiveSectionsByPhmAreaId(phmAreaId));
    }

    @GetMapping("/all/active")
    public ResponseEntity<List<SectionResponse>> getAllActiveSections() {
        return ResponseEntity.ok(sectionService.getAllActiveSections());
    }

    @PutMapping("/{sectionId}")
    public ResponseEntity<SectionResponse> update(
            @PathVariable Integer sectionId,
            @RequestBody SectionUpdateRequest request) {
        return ResponseEntity.ok(sectionService.update(sectionId, request));
    }

    @PutMapping("/{sectionId}/deactivate")
    public ResponseEntity<SectionResponse> deactivate(@PathVariable Integer sectionId) {
        return ResponseEntity.ok(sectionService.deactivate(sectionId));
    }

    @DeleteMapping("/{sectionId}")
    public ResponseEntity<Void> delete(@PathVariable Integer sectionId) {
        sectionService.delete(sectionId);
        return ResponseEntity.noContent().build();
    }
}
