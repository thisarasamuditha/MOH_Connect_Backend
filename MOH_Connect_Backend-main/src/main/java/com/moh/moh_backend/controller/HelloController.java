package com.moh.moh_backend.controller;// Meka oyage project eke package name ekata hadanna

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    @GetMapping("/")
    public String index() {
        return "MOH Backend is running on port 8082!";
    }
}