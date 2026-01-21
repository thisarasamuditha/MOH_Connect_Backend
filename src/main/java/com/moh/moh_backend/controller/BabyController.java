package com.moh.moh_backend.controller;

import com.moh.moh_backend.model.Baby;
import com.moh.moh_backend.service.BabyService;
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
    public ResponseEntity<Baby> create(@RequestBody Baby baby) {
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
