package com.moh.moh_backend.controller;

import com.moh.moh_backend.dto.MotherRegisterRequest;
import com.moh.moh_backend.service.MotherService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/mothers")
public class MotherController {
    private final MotherService motherService;

    public MotherController(MotherService motherService) {
        this.motherService = motherService;
    }

    //The midwife registers a new mother
    @PostMapping("/register")
    public ResponseEntity<?> registerMother(@RequestBody MotherRegisterRequest req) {
        motherService.registerMother(req);
        return ResponseEntity.ok("Mother registered successfully");
    }
}