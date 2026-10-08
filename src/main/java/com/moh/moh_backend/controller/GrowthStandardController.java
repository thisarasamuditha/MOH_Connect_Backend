package com.moh.moh_backend.controller;

import com.moh.moh_backend.model.Baby;
import com.moh.moh_backend.util.WhoWeightForAge;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Locale;
import java.util.Map;

/**
 * WHO reference curves used by the growth charts, so the UI and the stored growth status
 * come from the same table. Available to any signed-in user.
 */
@RestController
@RequestMapping("/growth-standards")
public class GrowthStandardController {

    /** Monthly -3/-2/0/+2/+3 SD weight-for-age curves, 0-60 months. */
    @GetMapping("/weight-for-age")
    public ResponseEntity<?> weightForAge(@RequestParam String sex) {
        Baby.Gender gender;
        try {
            gender = Baby.Gender.valueOf(sex.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "sex must be MALE or FEMALE"));
        }
        return ResponseEntity.ok(Map.of(
                "indicator", "weight-for-age",
                "sex", gender.name(),
                "unit", "kg",
                "source", "WHO Child Growth Standards",
                "points", WhoWeightForAge.monthlyCurve(gender)));
    }
}
