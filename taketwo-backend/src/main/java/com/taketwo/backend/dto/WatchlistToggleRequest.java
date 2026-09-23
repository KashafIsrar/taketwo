package com.taketwo.backend.dto;

import jakarta.validation.constraints.NotNull;

public record WatchlistToggleRequest(
        @NotNull Long tmdbId
) {}
