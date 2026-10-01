package com.taketwo.backend.controller;

import com.taketwo.backend.service.GamificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/gamification")
@RequiredArgsConstructor
public class GamificationController {

    private final GamificationService gamificationService;

    @GetMapping("/profile/{userId}")
    public Map<String, Object> getProfile(@PathVariable Long userId) {
        return gamificationService.getUserGamificationProfile(userId);
    }
}