package com.moh.moh_backend.controller;

import com.moh.moh_backend.dto.NotificationDtos.NotificationCreateRequest;
import com.moh.moh_backend.dto.NotificationDtos.NotificationResponse;
import com.moh.moh_backend.dto.NotificationDtos.NotificationStatusUpdate;
import com.moh.moh_backend.dto.NotificationDtos.UnreadCountResponse;
import com.moh.moh_backend.service.NotificationService;
import com.moh.moh_backend.config.RequireRoles;
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
    @RequireRoles({"ADMIN", "MIDWIFE", "DOCTOR"})
    public ResponseEntity<NotificationResponse> create(@RequestBody NotificationCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(notificationService.create(request));
    }

    @GetMapping("/{id}")
    @RequireRoles({"ADMIN", "MIDWIFE", "DOCTOR", "MOTHER"})
    public ResponseEntity<NotificationResponse> getById(@PathVariable Integer id, jakarta.servlet.http.HttpServletRequest request) {
        return ResponseEntity.ok(notificationService.getById(id, userId(request), role(request)));
    }

    @GetMapping("/mother/{motherId}")
    @RequireRoles({"ADMIN", "MIDWIFE", "DOCTOR", "MOTHER"})
    public ResponseEntity<List<NotificationResponse>> getByMother(@PathVariable Integer motherId, jakarta.servlet.http.HttpServletRequest request) {
        return ResponseEntity.ok(notificationService.getByMotherId(motherId, userId(request), role(request)));
    }

    @GetMapping("/mother/{motherId}/unread")
    @RequireRoles({"ADMIN", "MIDWIFE", "DOCTOR", "MOTHER"})
    public ResponseEntity<List<NotificationResponse>> getUnreadByMother(@PathVariable Integer motherId, jakarta.servlet.http.HttpServletRequest request) {
        return ResponseEntity.ok(notificationService.getUnreadByMotherId(motherId, userId(request), role(request)));
    }

    @GetMapping("/mother/{motherId}/unread-count")
    @RequireRoles({"ADMIN", "MIDWIFE", "DOCTOR", "MOTHER"})
    public ResponseEntity<UnreadCountResponse> getUnreadCount(@PathVariable Integer motherId, jakarta.servlet.http.HttpServletRequest request) {
        return ResponseEntity.ok(notificationService.getUnreadCount(motherId, userId(request), role(request)));
    }

    @GetMapping("/midwife/{midwifeId}")
    @RequireRoles({"ADMIN", "MIDWIFE"})
    public ResponseEntity<List<NotificationResponse>> getByMidwife(@PathVariable Integer midwifeId) {
        return ResponseEntity.ok(notificationService.getByMidwifeId(midwifeId));
    }

    @GetMapping("/status/{status}")
    @RequireRoles({"ADMIN", "MIDWIFE", "DOCTOR"})
    public ResponseEntity<List<NotificationResponse>> getByStatus(@PathVariable String status) {
        return ResponseEntity.ok(notificationService.getByStatus(status));
    }

    @PutMapping("/{id}/read")
    @RequireRoles({"ADMIN", "MIDWIFE", "DOCTOR", "MOTHER"})
    public ResponseEntity<NotificationResponse> markAsRead(@PathVariable Integer id, jakarta.servlet.http.HttpServletRequest request) {
        return ResponseEntity.ok(notificationService.markAsRead(id, userId(request), role(request)));
    }

    @PutMapping("/{id}/responded")
    @RequireRoles({"ADMIN", "MIDWIFE", "DOCTOR", "MOTHER"})
    public ResponseEntity<NotificationResponse> markAsResponded(@PathVariable Integer id, jakarta.servlet.http.HttpServletRequest request) {
        return ResponseEntity.ok(notificationService.markAsResponded(id, userId(request), role(request)));
    }

    @PutMapping("/{id}/status")
    @RequireRoles({"ADMIN", "MIDWIFE", "DOCTOR"})
    public ResponseEntity<NotificationResponse> updateStatus(
            @PathVariable Integer id,
            @RequestBody NotificationStatusUpdate request) {
        return ResponseEntity.ok(notificationService.updateStatus(id, request.getStatus()));
    }

    @DeleteMapping("/{id}")
    @RequireRoles("ADMIN")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        notificationService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private Integer userId(jakarta.servlet.http.HttpServletRequest request) {
        return (Integer) request.getAttribute("moh.userId");
    }

    private String role(jakarta.servlet.http.HttpServletRequest request) {
        return (String) request.getAttribute("moh.role");
    }
}
