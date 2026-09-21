package com.moh.moh_backend.controller;

import com.moh.moh_backend.dto.SessionDtos.SessionCreateRequest;
import com.moh.moh_backend.dto.SessionDtos.SessionResponse;
import com.moh.moh_backend.dto.SessionDtos.SessionUpdateRequest;
import com.moh.moh_backend.service.ClinicSessionService;
import com.moh.moh_backend.config.RequireRoles;
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
    @RequireRoles({"MIDWIFE", "ADMIN"})
    public ResponseEntity<SessionResponse> create(@RequestBody SessionCreateRequest request, jakarta.servlet.http.HttpServletRequest httpRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(sessionService.create(request,
                (Integer) httpRequest.getAttribute("moh.userId"),
                (String) httpRequest.getAttribute("moh.role")));
    }

    @GetMapping("/{sessionId}")
    @RequireRoles({"MIDWIFE", "ADMIN", "DOCTOR", "MOTHER"})
    public ResponseEntity<SessionResponse> getById(@PathVariable Integer sessionId) {
        return ResponseEntity.ok(sessionService.getById(sessionId));
    }

    @GetMapping("/midwife/{midwifeId}")
    @RequireRoles({"MIDWIFE", "ADMIN", "DOCTOR", "MOTHER"})
    public ResponseEntity<List<SessionResponse>> getByMidwife(@PathVariable Integer midwifeId) {
        return ResponseEntity.ok(sessionService.getByMidwifeId(midwifeId));
    }

    @GetMapping("/midwife/{midwifeId}/scheduled")
    @RequireRoles({"MIDWIFE", "ADMIN", "DOCTOR", "MOTHER"})
    public ResponseEntity<List<SessionResponse>> getScheduledByMidwife(@PathVariable Integer midwifeId) {
        return ResponseEntity.ok(sessionService.getScheduledByMidwife(midwifeId));
    }

    @GetMapping("/phm-area/{phmAreaId}")
    @RequireRoles({"MIDWIFE", "ADMIN", "DOCTOR", "MOTHER"})
    public ResponseEntity<List<SessionResponse>> getByPhmArea(@PathVariable Integer phmAreaId) {
        return ResponseEntity.ok(sessionService.getByPhmAreaId(phmAreaId));
    }

    @GetMapping("/status/{status}")
    @RequireRoles({"MIDWIFE", "ADMIN", "DOCTOR", "MOTHER"})
    public ResponseEntity<List<SessionResponse>> getByStatus(@PathVariable String status) {
        return ResponseEntity.ok(sessionService.getByStatus(status));
    }

    @GetMapping("/date-range")
    @RequireRoles({"MIDWIFE", "ADMIN", "DOCTOR", "MOTHER"})
    public ResponseEntity<List<SessionResponse>> getByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(sessionService.getByDateRange(from, to));
    }

    @PutMapping("/{sessionId}")
    @RequireRoles({"MIDWIFE", "ADMIN"})
    public ResponseEntity<SessionResponse> update(
            @PathVariable Integer sessionId,
            @RequestBody SessionUpdateRequest request,
            jakarta.servlet.http.HttpServletRequest httpRequest) {
        return ResponseEntity.ok(sessionService.update(sessionId, request,
                (Integer) httpRequest.getAttribute("moh.userId"),
                (String) httpRequest.getAttribute("moh.role")));
    }

    @DeleteMapping("/{sessionId}")
    @RequireRoles({"MIDWIFE", "ADMIN"})
    public ResponseEntity<Void> delete(@PathVariable Integer sessionId, jakarta.servlet.http.HttpServletRequest httpRequest) {
        sessionService.delete(sessionId,
                (Integer) httpRequest.getAttribute("moh.userId"),
                (String) httpRequest.getAttribute("moh.role"));
        return ResponseEntity.noContent().build();
    }
}
