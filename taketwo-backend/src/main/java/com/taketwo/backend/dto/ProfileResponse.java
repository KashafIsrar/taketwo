package com.taketwo.backend.dto;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public record ProfileResponse(
        UUID userId,
        String username,
        String displayName,
        String bio,
        String profilePictureUrl,
        long totalLogged,
        Double averageRating,   // null if the user hasn't logged anything yet
        long followerCount,
        long followingCount,
        List<Map<String, Object>> favoriteMovies
        Boolean isFollowing
) {}