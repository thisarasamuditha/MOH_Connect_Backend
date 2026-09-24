package com.moh.moh_backend.controller;

import com.moh.moh_backend.dto.MotherTriposhDistributionDtos.CreateRequest;
import com.moh.moh_backend.dto.MotherTriposhDistributionDtos.Response;
import com.moh.moh_backend.service.MotherTriposhDistributionService;
import com.moh.moh_backend.config.RequireRoles;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/triposha/mother")
@RequiredArgsConstructor
public class MotherTriposhDistributionController {

    private final MotherTriposhDistributionService distributionService;

    @PostMapping
    @RequireRoles({"MIDWIFE", "ADMIN"})
    public ResponseEntity<Response> distribute(@RequestBody CreateRequest request, jakarta.servlet.http.HttpServletRequest httpRequest) {
        Response response = distributionService.distribute(request, userId(httpRequest), role(httpRequest));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/pregnancy/{pregnancyId}")
    @RequireRoles({"ADMIN", "MIDWIFE", "DOCTOR", "MOTHER"})
    public ResponseEntity<List<Response>> getByPregnancy(@PathVariable Integer pregnancyId, jakarta.servlet.http.HttpServletRequest httpRequest) {
        return ResponseEntity.ok(distributionService.getByPregnancyId(pregnancyId, userId(httpRequest), role(httpRequest)));
    }

    @GetMapping("/mother/{motherId}")
    @RequireRoles({"ADMIN", "MIDWIFE", "DOCTOR", "MOTHER"})
    public ResponseEntity<List<Response>> getByMother(@PathVariable Integer motherId, jakarta.servlet.http.HttpServletRequest httpRequest) {
        return ResponseEntity.ok(distributionService.getByMotherId(motherId, userId(httpRequest), role(httpRequest)));
    }

    private Integer userId(jakarta.servlet.http.HttpServletRequest request) { return (Integer) request.getAttribute("moh.userId"); }
    private String role(jakarta.servlet.http.HttpServletRequest request) { return (String) request.getAttribute("moh.role"); }
}
