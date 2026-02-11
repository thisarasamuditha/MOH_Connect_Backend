package com.moh.moh_backend.controller;

import com.moh.moh_backend.dto.NotificationDtos.NotificationCreateRequest;
import com.moh.moh_backend.dto.NotificationDtos.NotificationResponse;
import com.moh.moh_backend.dto.NotificationDtos.NotificationStatusUpdate;
import com.moh.moh_backend.dto.NotificationDtos.UnreadCountResponse;
import com.moh.moh_backend.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping
    public ResponseEntity<NotificationResponse> create(@RequestBody NotificationCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(notificationService.create(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<NotificationResponse> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(notificationService.getById(id));
    }

    @GetMapping("/mother/{motherId}")
    public ResponseEntity<List<NotificationResponse>> getByMother(@PathVariable Integer motherId) {
        return ResponseEntity.ok(notificationService.getByMotherId(motherId));
    }

    @GetMapping("/mother/{motherId}/unread")
    public ResponseEntity<List<NotificationResponse>> getUnreadByMother(@PathVariable Integer motherId) {
        return ResponseEntity.ok(notificationService.getUnreadByMotherId(motherId));
    }

    @GetMapping("/mother/{motherId}/unread-count")
    public ResponseEntity<UnreadCountResponse> getUnreadCount(@PathVariable Integer motherId) {
        return ResponseEntity.ok(notificationService.getUnreadCount(motherId));
    }

    @GetMapping("/midwife/{midwifeId}")
    public ResponseEntity<List<NotificationResponse>> getByMidwife(@PathVariable Integer midwifeId) {
        return ResponseEntity.ok(notificationService.getByMidwifeId(midwifeId));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<NotificationResponse>> getByStatus(@PathVariable String status) {
        return ResponseEntity.ok(notificationService.getByStatus(status));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<NotificationResponse> markAsRead(@PathVariable Integer id) {
        return ResponseEntity.ok(notificationService.markAsRead(id));
    }

    @PutMapping("/{id}/responded")
    public ResponseEntity<NotificationResponse> markAsResponded(@PathVariable Integer id) {
        return ResponseEntity.ok(notificationService.markAsResponded(id));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<NotificationResponse> updateStatus(
            @PathVariable Integer id,
            @RequestBody NotificationStatusUpdate request) {
        return ResponseEntity.ok(notificationService.updateStatus(id, request.getStatus()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        notificationService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
