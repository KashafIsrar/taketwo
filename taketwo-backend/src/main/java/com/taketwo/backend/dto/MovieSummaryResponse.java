package com.taketwo.backend.dto;

import java.time.LocalDate;
import java.util.UUID;

public record MovieSummaryResponse(
        UUID id,            // null if this movie hasn't been cached locally yet
        Long tmdbId,
        String title,
        String posterUrl,
        LocalDate releaseDate,
        Double voteAverage
) {}
