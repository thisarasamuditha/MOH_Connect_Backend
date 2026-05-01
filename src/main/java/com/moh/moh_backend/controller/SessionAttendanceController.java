package com.moh.moh_backend.controller;

import com.moh.moh_backend.dto.SessionDtos.AttendanceCreateRequest;
import com.moh.moh_backend.dto.SessionDtos.AttendanceMarkRequest;
import com.moh.moh_backend.dto.SessionDtos.AttendanceResponse;
import com.moh.moh_backend.service.SessionAttendanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/session-attendance")
@RequiredArgsConstructor
public class SessionAttendanceController {

    private final SessionAttendanceService attendanceService;

    @PostMapping
    public ResponseEntity<AttendanceResponse> register(@RequestBody AttendanceCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(attendanceService.register(request));
    }

    @PutMapping("/{attendanceId}/mark")
    public ResponseEntity<AttendanceResponse> markAttendance(
            @PathVariable Integer attendanceId,
            @RequestBody AttendanceMarkRequest request) {
        return ResponseEntity.ok(attendanceService.markAttendance(attendanceId, request));
    }

    @GetMapping("/session/{sessionId}")
    public ResponseEntity<List<AttendanceResponse>> getBySession(@PathVariable Integer sessionId) {
        return ResponseEntity.ok(attendanceService.getBySessionId(sessionId));
    }

    @GetMapping("/mother/{motherId}")
    public ResponseEntity<List<AttendanceResponse>> getByMother(@PathVariable Integer motherId) {
        return ResponseEntity.ok(attendanceService.getByMotherId(motherId));
    }

    @DeleteMapping("/{attendanceId}")
    public ResponseEntity<Void> delete(@PathVariable Integer attendanceId) {
        attendanceService.delete(attendanceId);
        return ResponseEntity.noContent().build();
    }
}
