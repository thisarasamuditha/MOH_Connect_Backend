package com.moh.moh_backend.controller;

import com.moh.moh_backend.dto.MotherTriposhDistributionDtos.CreateRequest;
import com.moh.moh_backend.dto.MotherTriposhDistributionDtos.Response;
import com.moh.moh_backend.service.MotherTriposhDistributionService;
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
    public ResponseEntity<Response> distribute(@RequestBody CreateRequest request) {
        Response response = distributionService.distribute(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/pregnancy/{pregnancyId}")
    public ResponseEntity<List<Response>> getByPregnancy(@PathVariable Integer pregnancyId) {
        return ResponseEntity.ok(distributionService.getByPregnancyId(pregnancyId));
    }

    @GetMapping("/mother/{motherId}")
    public ResponseEntity<List<Response>> getByMother(@PathVariable Integer motherId) {
        return ResponseEntity.ok(distributionService.getByMotherId(motherId));
    }
}
