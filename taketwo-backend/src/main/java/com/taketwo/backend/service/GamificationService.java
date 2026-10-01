package com.taketwo.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class GamificationService {

    // Inject your real repositories here when ready, e.g.:
    // private final MovieLogRepository movieLogRepository;
    // private final ReviewRepository reviewRepository;

    public Map<String, Object> getUserGamificationProfile(Long userId) {
        // Example of fetching real counts from repositories:
        // long totalLogged = movieLogRepository.countByUserId(userId);
        // int currentStreak = calculateStreak(userId);

        long totalLogged = 42; // Replace with movieLogRepository.countByUserId(userId)
        int currentStreak = 5;  // Replace with dynamic streak calculation logic
        int longestStreak = 14;

        List<Map<String, Object>> badges = List.of(
            Map.of("id", "horror_fan", "title", "Nightmare Fiend", "description", "Logged 10 Horror Movies", "unlocked", totalLogged >= 10, "status", totalLogged >= 10 ? "Unlocked" : "Locked"),
            Map.of("id", "reviewer_50", "title", "Critique Master", "description", "Wrote 50 Reviews", "unlocked", false, "status", "Locked"),
            Map.of("id", "streak_7", "title", "Dedicated Cinephile", "description", "Maintained a 7-day watch streak", "unlocked", currentStreak >= 7, "status", currentStreak >= 7 ? "Unlocked" : "Locked")
        );

        List<Map<String, Object>> challenges = List.of(
            Map.of("title", "1980s Nostalgia", "goal", 5, "current", 3, "description", "Watch 5 films from the 1980s this month", "deadline", "October 31, 2026")
        );

        return Map.of(
            "currentStreakDays", currentStreak,
            "longestStreak", longestStreak,
            "totalLogged", totalLogged,
            "badges", badges,
            "activeChallenges", challenges
        );
    }
}