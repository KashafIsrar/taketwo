package com.taketwo.backend.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record ActivityFeedItemResponse(
        UUID logId,
        FollowUserSummary user,
        MovieSummaryResponse movie,
        LocalDate watchedDate,
        Double rating,
        boolean rewatch,
        String reviewText,
        Boolean containsSpoilers,
        Instant createdAt
) {}
