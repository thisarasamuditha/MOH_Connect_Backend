package com.moh.moh_backend.controller;

import com.moh.moh_backend.model.PhmArea;
import com.moh.moh_backend.repository.PhmAreaRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/phm-areas")
public class PhmAreaController {

    private final PhmAreaRepository phmAreaRepository;

    public PhmAreaController(PhmAreaRepository phmAreaRepository) {
        this.phmAreaRepository = phmAreaRepository;
    }

    /**
     * Get all PHM Areas
     */
    @GetMapping
    public List<PhmArea> getAllPhmAreas() {
        return phmAreaRepository.findAll();
    }

    /**
     * Get PHM Area by ID
     */
    @GetMapping("/{id}")
    public PhmArea getPhmAreaById(@PathVariable Integer id) {
        return phmAreaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("PHM Area not found with ID: " + id));
    }

    /**
     * Create a new PHM Area
     */
    @PostMapping
    public PhmArea createPhmArea(@RequestBody PhmArea phmArea) {
        if (phmAreaRepository.findByAreaCode(phmArea.getAreaCode()).isPresent()) {
            throw new IllegalArgumentException("Area code already exists: " + phmArea.getAreaCode());
        }
        return phmAreaRepository.save(phmArea);
    }

    /**
     * Update PHM Area
     */
    @PutMapping("/{id}")
    public PhmArea updatePhmArea(@PathVariable Integer id, @RequestBody PhmArea phmAreaDetails) {
        PhmArea phmArea = phmAreaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("PHM Area not found with ID: " + id));

        phmArea.setAreaName(phmAreaDetails.getAreaName());
        phmArea.setAreaCode(phmAreaDetails.getAreaCode());

        return phmAreaRepository.save(phmArea);
    }

    /**
     * Delete PHM Area
     */
    @DeleteMapping("/{id}")
    public void deletePhmArea(@PathVariable Integer id) {
        if (!phmAreaRepository.existsById(id)) {
            throw new IllegalArgumentException("PHM Area not found with ID: " + id);
        }
        phmAreaRepository.deleteById(id);
    }
}
