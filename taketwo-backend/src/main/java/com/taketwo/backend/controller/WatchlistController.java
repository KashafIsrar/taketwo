package com.taketwo.backend.controller;

import com.taketwo.backend.dto.WatchlistItemResponse;
import com.taketwo.backend.dto.WatchlistToggleRequest;
import com.taketwo.backend.dto.WatchlistToggleResponse;
import com.taketwo.backend.service.CurrentUserService;
import com.taketwo.backend.service.WatchlistService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/movies/watchlist")
@RequiredArgsConstructor
public class WatchlistController {

    private final WatchlistService watchlistService;
    private final CurrentUserService currentUserService;

    // Single endpoint toggles add/remove - if it's already on the list, this takes it off.
    @PostMapping
    public WatchlistToggleResponse toggle(@Valid @RequestBody WatchlistToggleRequest request, Authentication authentication) {
        var user = currentUserService.getCurrentUser(authentication);
        return watchlistService.toggle(user, request.tmdbId());
    }

    @GetMapping("/user/{userId}")
    public List<WatchlistItemResponse> getUserWatchlist(@PathVariable UUID userId) {
        return watchlistService.getUserWatchlist(userId);
    }

    // Authenticated (not just "public GET") because it answers "is this on
    // *my* watchlist" - it needs to know who's asking.
    @GetMapping("/status/{tmdbId}")
    public java.util.Map<String, Boolean> status(@PathVariable Long tmdbId, Authentication authentication) {
        var user = currentUserService.getCurrentUser(authentication);
        return java.util.Map.of("onWatchlist", watchlistService.isOnWatchlist(user.getId(), tmdbId));
    }
}
