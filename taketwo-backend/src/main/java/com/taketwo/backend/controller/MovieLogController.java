package com.taketwo.backend.controller;

import com.taketwo.backend.dto.LogMovieRequest;
import com.taketwo.backend.dto.MovieLogResponse;
import com.taketwo.backend.service.CurrentUserService;
import com.taketwo.backend.service.MovieLogService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/movies/logs")
@RequiredArgsConstructor
public class MovieLogController {

    private final MovieLogService movieLogService;
    private final CurrentUserService currentUserService;

    @PostMapping
    public MovieLogResponse create(@Valid @RequestBody LogMovieRequest request, Authentication authentication) {
        var user = currentUserService.getCurrentUser(authentication);
        return movieLogService.createLog(user, request);
    }

    // Public - this is what powers a user's diary/profile page.
    @GetMapping("/user/{userId}")
    public List<MovieLogResponse> getUserLogs(@PathVariable UUID userId) {
        return movieLogService.getUserLogs(userId);
    }
}
