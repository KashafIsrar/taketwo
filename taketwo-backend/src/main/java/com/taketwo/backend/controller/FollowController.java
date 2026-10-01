package com.taketwo.backend.controller;

import com.taketwo.backend.dto.FollowStatusResponse;
import com.taketwo.backend.dto.FollowUserSummary;
import com.taketwo.backend.service.CurrentUserService;
import com.taketwo.backend.service.FollowService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class FollowController {

    private final FollowService followService;
    private final CurrentUserService currentUserService;

    // Single endpoint toggles follow/unfollow, same pattern as the watchlist toggle.
    @PostMapping("/{userId}/follow")
    public FollowStatusResponse toggleFollow(@PathVariable UUID userId, Authentication authentication) {
        var currentUser = currentUserService.getCurrentUser(authentication);
        return followService.toggleFollow(currentUser, userId);
    }

    // Authenticated - needs to know who's asking, same reason watchlist/status is authenticated.
    @GetMapping("/{userId}/follow-status")
    public FollowStatusResponse followStatus(@PathVariable UUID userId, Authentication authentication) {
        var currentUser = currentUserService.getCurrentUser(authentication);
        return followService.getStatus(currentUser, userId);
    }

    @GetMapping("/{userId}/followers")
    public List<FollowUserSummary> followers(@PathVariable UUID userId) {
        return followService.getFollowers(userId);
    }

    @GetMapping("/{userId}/following")
    public List<FollowUserSummary> following(@PathVariable UUID userId) {
        return followService.getFollowing(userId);
    }
}