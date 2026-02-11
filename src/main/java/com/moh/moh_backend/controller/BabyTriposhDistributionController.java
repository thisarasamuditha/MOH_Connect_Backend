package com.moh.moh_backend.controller;

import com.moh.moh_backend.dto.BabyTriposhDistributionDtos.CreateRequest;
import com.moh.moh_backend.dto.BabyTriposhDistributionDtos.Response;
import com.moh.moh_backend.service.BabyTriposhDistributionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/triposha/baby")
@RequiredArgsConstructor
public class BabyTriposhDistributionController {

    private final BabyTriposhDistributionService distributionService;

    @PostMapping
    public ResponseEntity<Response> distribute(@RequestBody CreateRequest request) {
        Response response = distributionService.distribute(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/baby/{babyId}")
    public ResponseEntity<List<Response>> getByBaby(@PathVariable Integer babyId) {
        return ResponseEntity.ok(distributionService.getByBabyId(babyId));
    }

    @GetMapping("/mother/{motherId}")
    public ResponseEntity<List<Response>> getByMother(@PathVariable Integer motherId) {
        return ResponseEntity.ok(distributionService.getByMotherId(motherId));
    }
}
