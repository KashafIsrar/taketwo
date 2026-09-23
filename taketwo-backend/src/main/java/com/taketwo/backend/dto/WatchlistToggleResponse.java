package com.taketwo.backend.dto;

public record WatchlistToggleResponse(
        boolean onWatchlist,
        MovieSummaryResponse movie
) {}
