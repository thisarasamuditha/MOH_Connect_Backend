package com.moh.moh_backend.controller;

import com.moh.moh_backend.model.Baby;
import com.moh.moh_backend.service.BabyService;
import com.moh.moh_backend.util.JwtService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/babies")
public class BabyController {

    private final BabyService babyService;
    private final JwtService jwtService;

    public BabyController(BabyService babyService, JwtService jwtService) {
        this.babyService = babyService;
        this.jwtService = jwtService;
    }

    @PostMapping
    public ResponseEntity<?> create(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody Baby baby) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body(Map.of("error", "Missing Bearer token"));
        }

        String token = authorization.substring("Bearer ".length()).trim();
        String role = jwtService.getRole(token);
        if (role == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid token"));
        }

        if (!"MIDWIFE".equalsIgnoreCase(role) && !"DOCTOR".equalsIgnoreCase(role)) {
            return ResponseEntity.status(403).body(Map.of("error", "Only midwives and doctors can add babies"));
        }

        Baby saved = babyService.save(baby);
        return ResponseEntity.created(URI.create("/api/babies/" + saved.getBabyId())).body(saved);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Baby> getById(@PathVariable Integer id) {
        return babyService.findById(id).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<Baby>> list(@RequestParam(required = false) Integer motherId,
                                           @RequestParam(required = false) Integer pregnancyId) {
        if (motherId != null) return ResponseEntity.ok(babyService.findByMotherId(motherId));
        if (pregnancyId != null) return ResponseEntity.ok(babyService.findByPregnancyId(pregnancyId));
        return ResponseEntity.ok(babyService.findByPregnancyId(null)); // or implement findAll in service if desired
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        babyService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
