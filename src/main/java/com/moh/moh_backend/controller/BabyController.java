package com.moh.moh_backend.controller;

import com.moh.moh_backend.model.Baby;
import com.moh.moh_backend.service.BabyService;
import com.moh.moh_backend.config.RequireRoles;
import com.moh.moh_backend.util.JwtService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.moh.moh_backend.util.JwtService;

import java.net.URI;
import java.util.List;
@Slf4j
@RestController
@RequestMapping("/babies")
public class BabyController {

    private final BabyService babyService;
    private final JwtService jwtService;

    public BabyController(BabyService babyService , JwtService jwtService) {
        this.babyService = babyService;
        this.jwtService  = jwtService;
    }

    @PostMapping
    @RequireRoles({"MIDWIFE", "DOCTOR"})
    public ResponseEntity<?> create( @RequestHeader("Authorization") String authorization,@RequestBody Baby baby, jakarta.servlet.http.HttpServletRequest request) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body("Missing Bearer token");
        }
        String token = authorization.substring("Bearer ".length()).trim();

        {
            Baby saved = babyService.save(baby,
                    jwtService.getUserId(token),
                    jwtService.getRole(token));
            return ResponseEntity.created(URI.create("/api/babies/" + saved.getBabyId())).body(saved);
        }
    }

    @GetMapping("/{id}")
    @RequireRoles({"MIDWIFE","MOTHER"})
    public ResponseEntity<Baby> getById(@PathVariable Integer id, jakarta.servlet.http.HttpServletRequest request) {
     Integer UserID =  (Integer) request.getAttribute("moh.userId");
        log.info("userId: {}", UserID);

        return babyService.findById(id,
                (Integer) request.getAttribute("moh.userId"),
                (String) request.getAttribute("moh.role"))
                .map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping
    @RequireRoles({"MIDWIFE", "DOCTOR", "MOTHER", "ADMIN"})
    public ResponseEntity<List<Baby>> list(@RequestParam(required = false) Integer motherId,
                                           @RequestParam(required = false) Integer pregnancyId,
                                           jakarta.servlet.http.HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("moh.userId");
        String role = (String) request.getAttribute("moh.role");
        if (motherId != null) return ResponseEntity.ok(babyService.findByMotherId(motherId, userId, role));
        if (pregnancyId != null) return ResponseEntity.ok(babyService.findByPregnancyId(pregnancyId, userId, role));
        return ResponseEntity.ok(babyService.findAll(userId, role));
    }

    @DeleteMapping("/{id}")
    @RequireRoles({"MIDWIFE", "DOCTOR"})
    public ResponseEntity<Void> delete(@PathVariable Integer id, jakarta.servlet.http.HttpServletRequest request) {
        babyService.deleteById(id,
                (Integer) request.getAttribute("moh.userId"),
                (String) request.getAttribute("moh.role"));
        return ResponseEntity.noContent().build();
    }
}
