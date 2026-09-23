package com.taketwo.backend.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record MovieLogResponse(
        UUID logId,
        MovieSummaryResponse movie,
        LocalDate watchedDate,
        Double rating,
        boolean rewatch,
        String reviewText,          // null if this log has no review
        Boolean containsSpoilers,   // null if this log has no review
        Instant createdAt
) {}
