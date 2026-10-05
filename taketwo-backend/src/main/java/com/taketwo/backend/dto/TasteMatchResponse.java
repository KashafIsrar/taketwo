// dto/TasteMatchResponse.java
package com.taketwo.backend.dto;

import java.util.List;
import java.util.UUID;

public record TasteMatchResponse(
        UUID targetUserId,
        String targetUsername,
        int matchPercent,
        List<SharedGenre> sharedGenres,
        List<SharedDirector> sharedDirectors,
        int commonlyWatchedCount,
        Double ratingAlignmentPercent,   // null if zero films in common - no alignment to measure
        List<MovieSummaryResponse> moviesTheyLovedYouHaventSeen
) {}