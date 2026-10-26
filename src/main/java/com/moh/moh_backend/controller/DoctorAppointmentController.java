package com.moh.moh_backend.controller;

import com.moh.moh_backend.config.RequireRoles;
import com.moh.moh_backend.dto.DoctorAppointmentDtos;
import com.moh.moh_backend.service.DoctorAppointmentService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/doctor-appointments")
@RequiredArgsConstructor
public class DoctorAppointmentController {

    private final DoctorAppointmentService appointmentService;

    @PostMapping
    @RequireRoles({"MIDWIFE", "DOCTOR", "ADMIN"})
    public ResponseEntity<?> createAppointment(
            @RequestBody DoctorAppointmentDtos.CreateRequest req,
            HttpServletRequest request) {
        try {
            Integer userId = (Integer) request.getAttribute("moh.userId");
            String role = (String) request.getAttribute("moh.role");
            DoctorAppointmentDtos.Response response = appointmentService.createAppointment(req, userId, role);
            return ResponseEntity
                    .created(URI.create("/api/doctor-appointments/" + response.getAppointmentId()))
                    .body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage() != null ? e.getMessage() : "Failed to create appointment"));
        }
    }

    @GetMapping
    @RequireRoles({"DOCTOR", "MIDWIFE", "ADMIN"})
    public ResponseEntity<?> getAppointments(
            @RequestParam(required = false) String status,
            HttpServletRequest request) {
        try {
            Integer userId = (Integer) request.getAttribute("moh.userId");
            String role = (String) request.getAttribute("moh.role");
            List<DoctorAppointmentDtos.Response> list = appointmentService.getAppointments(status, userId, role);
            return ResponseEntity.ok(list);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage() != null ? e.getMessage() : "Failed to fetch appointments"));
        }
    }

    @GetMapping("/consultations")
    @RequireRoles({"DOCTOR", "ADMIN"})
    public ResponseEntity<?> getConsultations(HttpServletRequest request) {
        try {
            Integer userId = (Integer) request.getAttribute("moh.userId");
            String role = (String) request.getAttribute("moh.role");
            List<DoctorAppointmentDtos.Response> list = appointmentService.getConsultations(userId, role);
            return ResponseEntity.ok(list);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage() != null ? e.getMessage() : "Failed to fetch consultations"));
        }
    }

    @GetMapping("/doctors")
    @RequireRoles({"MIDWIFE", "DOCTOR", "ADMIN"})
    public ResponseEntity<?> getDoctors() {
        try {
            List<DoctorAppointmentDtos.DoctorInfo> doctors = appointmentService.getAllDoctors();
            return ResponseEntity.ok(doctors);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage() != null ? e.getMessage() : "Failed to fetch doctors list"));
        }
    }

    @GetMapping("/{id}")
    @RequireRoles({"DOCTOR", "MIDWIFE", "ADMIN"})
    public ResponseEntity<?> getAppointmentById(
            @PathVariable Integer id,
            HttpServletRequest request) {
        try {
            Integer userId = (Integer) request.getAttribute("moh.userId");
            String role = (String) request.getAttribute("moh.role");
            DoctorAppointmentDtos.Response response = appointmentService.getAppointmentById(id, userId, role);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage() != null ? e.getMessage() : "Failed to fetch appointment"));
        }
    }

    @PutMapping("/{id}/schedule")
    @RequireRoles({"DOCTOR", "ADMIN"})
    public ResponseEntity<?> scheduleAppointment(
            @PathVariable Integer id,
            @RequestBody DoctorAppointmentDtos.ScheduleRequest req,
            HttpServletRequest request) {
        try {
            Integer userId = (Integer) request.getAttribute("moh.userId");
            String role = (String) request.getAttribute("moh.role");
            DoctorAppointmentDtos.Response response = appointmentService.scheduleAppointment(id, req, userId, role);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage() != null ? e.getMessage() : "Failed to schedule appointment"));
        }
    }

    @PutMapping("/{id}/complete")
    @RequireRoles({"DOCTOR", "ADMIN"})
    public ResponseEntity<?> completeConsultation(
            @PathVariable Integer id,
            @RequestBody DoctorAppointmentDtos.CompleteConsultationRequest req,
            HttpServletRequest request) {
        try {
            Integer userId = (Integer) request.getAttribute("moh.userId");
            String role = (String) request.getAttribute("moh.role");
            DoctorAppointmentDtos.Response response = appointmentService.completeConsultation(id, req, userId, role);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage() != null ? e.getMessage() : "Failed to complete consultation"));
        }
    }
}
