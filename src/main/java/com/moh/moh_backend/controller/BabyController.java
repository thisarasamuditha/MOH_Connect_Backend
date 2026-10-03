package com.moh.moh_backend.controller;

import com.moh.moh_backend.dto.BabyResponseDto;
import com.moh.moh_backend.model.Baby;
import com.moh.moh_backend.service.BabyService;
import com.moh.moh_backend.config.RequireRoles;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/babies")
public class BabyController {

    private final BabyService babyService;

    public BabyController(BabyService babyService) {
        this.babyService = babyService;
    }

    @PostMapping
    @RequireRoles({"MIDWIFE", "DOCTOR"})
    public ResponseEntity<BabyResponseDto> create(@RequestBody Baby baby, jakarta.servlet.http.HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("moh.userId");
        String role = (String) request.getAttribute("moh.role");
        Baby saved = babyService.save(baby, userId, role);
        BabyResponseDto dto = babyService.toDto(saved);
        return ResponseEntity.created(URI.create("/api/babies/" + saved.getBabyId())).body(dto);
    }

    @PutMapping("/{id}")
    @RequireRoles({"MIDWIFE", "DOCTOR"})
    public ResponseEntity<BabyResponseDto> update(@PathVariable Integer id,
                                                  @RequestBody Baby baby,
                                                  jakarta.servlet.http.HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("moh.userId");
        String role = (String) request.getAttribute("moh.role");
        Baby updated = babyService.update(id, baby, userId, role);
        return ResponseEntity.ok(babyService.toDto(updated));
    }

    @GetMapping("/{id}")
    @RequireRoles({"MIDWIFE", "DOCTOR", "MOTHER"})
    public ResponseEntity<BabyResponseDto> getById(@PathVariable Integer id, jakarta.servlet.http.HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("moh.userId");
        String role = (String) request.getAttribute("moh.role");
        return babyService.findDtoById(id, userId, role)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping
    @RequireRoles({"MIDWIFE", "DOCTOR", "MOTHER", "ADMIN"})
    public ResponseEntity<List<BabyResponseDto>> list(@RequestParam(required = false) Integer motherId,
                                                      @RequestParam(required = false) Integer pregnancyId,
                                                      jakarta.servlet.http.HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("moh.userId");
        String role = (String) request.getAttribute("moh.role");
        if (motherId != null) return ResponseEntity.ok(babyService.findDtosByMotherId(motherId, userId, role));
        if (pregnancyId != null) return ResponseEntity.ok(babyService.findDtosByPregnancyId(pregnancyId, userId, role));
        return ResponseEntity.ok(babyService.findAllDtos(userId, role));
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
