package com.taketwo.backend.controller;

import com.taketwo.backend.dto.ActivityFeedItemResponse;
import com.taketwo.backend.service.ActivityFeedService;
import com.taketwo.backend.service.CurrentUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ActivityFeedController {

    private final ActivityFeedService activityFeedService;
    private final CurrentUserService currentUserService;

    // Authenticated by default via the anyRequest().authenticated() catch-all -
    // no new SecurityConfig rule needed, see note below.
    @GetMapping("/api/feed")
    public List<ActivityFeedItemResponse> getFeed(
            @RequestParam(required = false) Integer limit,
            Authentication authentication
    ) {
        var currentUser = currentUserService.getCurrentUser(authentication);
        return activityFeedService.getFeed(currentUser, limit);
    }
}
