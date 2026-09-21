package com.moh.moh_backend.controller;

import com.moh.moh_backend.model.TargetGroup;
import com.moh.moh_backend.model.VaccineSchedule;
import com.moh.moh_backend.service.VaccineScheduleService;
import com.moh.moh_backend.config.RequireRoles;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/vaccine-schedules")
public class VaccineScheduleController {

    private final VaccineScheduleService vaccineScheduleService;

    public VaccineScheduleController(VaccineScheduleService vaccineScheduleService) {
        this.vaccineScheduleService = vaccineScheduleService;
    }

    @PostMapping
    @RequireRoles("ADMIN")
    public ResponseEntity<?> createSchedule(@RequestBody VaccineSchedule schedule) {
        try {
            VaccineSchedule created = vaccineScheduleService.createSchedule(schedule);
            return ResponseEntity
                    .created(URI.create("/api/vaccine-schedules/" + created.getScheduleId()))
                    .body(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping("/{id}")
    @RequireRoles({"ADMIN", "MIDWIFE", "DOCTOR", "MOTHER"})
    public ResponseEntity<?> getScheduleById(@PathVariable Integer id) {
        try {
            VaccineSchedule schedule = vaccineScheduleService.getScheduleById(id);
            return ResponseEntity.ok(schedule);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @GetMapping
    @RequireRoles({"ADMIN", "MIDWIFE", "DOCTOR", "MOTHER"})
    public ResponseEntity<?> getAllSchedules() {
        try {
            List<VaccineSchedule> schedules = vaccineScheduleService.getAllSchedules();
            return ResponseEntity.ok(schedules);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping("/by-target-group/{targetGroup}")
    @RequireRoles({"ADMIN", "MIDWIFE", "DOCTOR", "MOTHER"})
    public ResponseEntity<?> getSchedulesByTargetGroup(@PathVariable String targetGroup) {
        try {
            TargetGroup target = TargetGroup.valueOf(targetGroup.toUpperCase());
            List<VaccineSchedule> schedules = vaccineScheduleService.getSchedulesByTargetGroup(target);
            return ResponseEntity.ok(schedules);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Invalid target group. Use MOTHER or BABY");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @RequireRoles("ADMIN")
    public ResponseEntity<?> updateSchedule(@PathVariable Integer id, 
                                           @RequestBody VaccineSchedule schedule) {
        try {
            VaccineSchedule updated = vaccineScheduleService.updateSchedule(id, schedule);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @RequireRoles("ADMIN")
    public ResponseEntity<?> deleteSchedule(@PathVariable Integer id) {
        try {
            vaccineScheduleService.deleteSchedule(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
}
