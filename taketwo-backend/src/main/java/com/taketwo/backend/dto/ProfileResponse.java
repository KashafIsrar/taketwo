package com.taketwo.backend.dto;

import java.util.UUID;

public record ProfileResponse(
        UUID userId,
        String username,
        String displayName,
        long totalLogged,
        Double averageRating,   // null if the user hasn't logged anything yet
        long followerCount,
        long followingCount
) {}
