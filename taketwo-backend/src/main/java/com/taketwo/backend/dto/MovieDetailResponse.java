package com.taketwo.backend.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record MovieDetailResponse(
        UUID id,
        Long tmdbId,
        String title,
        String overview,
        String posterUrl,
        LocalDate releaseDate,
        Integer runtimeMinutes,
        Double voteAverage,
        List<String> genres
) {}
