package com.taketwo.backend.dto;

import java.time.Instant;

public record WatchlistItemResponse(
        MovieSummaryResponse movie,
        Instant addedAt
) {}
