package com.moh.moh_backend.controller;

import com.moh.moh_backend.dto.SessionDtos.SessionCreateRequest;
import com.moh.moh_backend.dto.SessionDtos.SessionResponse;
import com.moh.moh_backend.dto.SessionDtos.SessionUpdateRequest;
import com.moh.moh_backend.service.ClinicSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/sessions")
@RequiredArgsConstructor
public class ClinicSessionController {

    private final ClinicSessionService sessionService;

    @PostMapping
    public ResponseEntity<SessionResponse> create(@RequestBody SessionCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(sessionService.create(request));
    }

    @GetMapping("/{sessionId}")
    public ResponseEntity<SessionResponse> getById(@PathVariable Integer sessionId) {
        return ResponseEntity.ok(sessionService.getById(sessionId));
    }

    @GetMapping("/midwife/{midwifeId}")
    public ResponseEntity<List<SessionResponse>> getByMidwife(@PathVariable Integer midwifeId) {
        return ResponseEntity.ok(sessionService.getByMidwifeId(midwifeId));
    }

    @GetMapping("/midwife/{midwifeId}/scheduled")
    public ResponseEntity<List<SessionResponse>> getScheduledByMidwife(@PathVariable Integer midwifeId) {
        return ResponseEntity.ok(sessionService.getScheduledByMidwife(midwifeId));
    }

    @GetMapping("/phm-area/{phmAreaId}")
    public ResponseEntity<List<SessionResponse>> getByPhmArea(@PathVariable Integer phmAreaId) {
        return ResponseEntity.ok(sessionService.getByPhmAreaId(phmAreaId));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<SessionResponse>> getByStatus(@PathVariable String status) {
        return ResponseEntity.ok(sessionService.getByStatus(status));
    }

    @GetMapping("/date-range")
    public ResponseEntity<List<SessionResponse>> getByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(sessionService.getByDateRange(from, to));
    }

    @PutMapping("/{sessionId}")
    public ResponseEntity<SessionResponse> update(
            @PathVariable Integer sessionId,
            @RequestBody SessionUpdateRequest request) {
        return ResponseEntity.ok(sessionService.update(sessionId, request));
    }

    @DeleteMapping("/{sessionId}")
    public ResponseEntity<Void> delete(@PathVariable Integer sessionId) {
        sessionService.delete(sessionId);
        return ResponseEntity.noContent().build();
    }
}
