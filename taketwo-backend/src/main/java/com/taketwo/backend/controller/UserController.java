package com.taketwo.backend.controller;

import com.taketwo.backend.dto.ProfileResponse;
import com.taketwo.backend.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final ProfileService profileService;

    // Public - profiles are meant to be browsable without an account.
    @GetMapping("/{userId}/profile")
    public ProfileResponse getProfile(@PathVariable UUID userId) {
        return profileService.getProfile(userId);
    }
}
