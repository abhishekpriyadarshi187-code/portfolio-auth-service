package com.abhishek.portfolio.auth.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    @GetMapping("/health")
    public String health() {
        return "Auth service is running";
    }

    @GetMapping("/profile")
    public String profile() {
        return "User profile";
    }
}
