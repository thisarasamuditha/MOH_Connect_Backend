package com.moh.moh_backend.controller;

import com.moh.moh_backend.model.SectionType;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/section-types")
@RequiredArgsConstructor
public class SectionTypeController {

    @GetMapping
    public ResponseEntity<List<SectionTypeDto>> getAllSectionTypes() {
        List<SectionTypeDto> types = Arrays.stream(SectionType.values())
                .map(type -> new SectionTypeDto(type.name(), type.getDisplayName()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(types);
    }

    public static class SectionTypeDto {
        public String value;
        public String label;

        public SectionTypeDto(String value, String label) {
            this.value = value;
            this.label = label;
        }
    }
}
