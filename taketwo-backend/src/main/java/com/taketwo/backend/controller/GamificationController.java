package com.taketwo.backend.controller;

import com.taketwo.backend.service.GamificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/gamification")
@RequiredArgsConstructor
public class GamificationController {

    private final GamificationService gamificationService;

    // @PathVariable UUID (not String) makes Spring reject a malformed id with
    // a clean 400 automatically, instead of silently stripping stray
    // characters and hoping for the best.
    @GetMapping("/profile/{userId}")
    public Map<String, Object> getProfile(@PathVariable UUID userId) {
        return gamificationService.getUserGamificationProfile(userId);
    }
}