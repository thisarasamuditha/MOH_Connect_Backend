package com.moh.moh_backend.controller;

import com.moh.moh_backend.dto.NotificationDtos.NotificationTypeRequest;
import com.moh.moh_backend.dto.NotificationDtos.NotificationTypeResponse;
import com.moh.moh_backend.service.NotificationTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notification-types")
@RequiredArgsConstructor
public class NotificationTypeController {

    private final NotificationTypeService typeService;

    @PostMapping
    public ResponseEntity<NotificationTypeResponse> create(@RequestBody NotificationTypeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(typeService.create(request));
    }

    @GetMapping
    public ResponseEntity<List<NotificationTypeResponse>> getAll() {
        return ResponseEntity.ok(typeService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<NotificationTypeResponse> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(typeService.getById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<NotificationTypeResponse> update(
            @PathVariable Integer id,
            @RequestBody NotificationTypeRequest request) {
        return ResponseEntity.ok(typeService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        typeService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
