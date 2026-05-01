package com.moh.moh_backend.controller;

import com.moh.moh_backend.dto.SessionDtos.SessionTypeRequest;
import com.moh.moh_backend.dto.SessionDtos.SessionTypeResponse;
import com.moh.moh_backend.service.SessionTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/session-types")
@RequiredArgsConstructor
public class SessionTypeController {

    private final SessionTypeService sessionTypeService;

    @PostMapping
    public ResponseEntity<SessionTypeResponse> create(@RequestBody SessionTypeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(sessionTypeService.create(request));
    }

    @GetMapping
    public ResponseEntity<List<SessionTypeResponse>> getAll() {
        return ResponseEntity.ok(sessionTypeService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SessionTypeResponse> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(sessionTypeService.getById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SessionTypeResponse> update(
            @PathVariable Integer id,
            @RequestBody SessionTypeRequest request) {
        return ResponseEntity.ok(sessionTypeService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        sessionTypeService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
