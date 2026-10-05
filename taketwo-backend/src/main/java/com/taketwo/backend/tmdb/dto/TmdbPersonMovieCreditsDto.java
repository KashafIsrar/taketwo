// tmdb/dto/TmdbPersonMovieCreditsDto.java
package com.taketwo.backend.tmdb.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

// Reuses TmdbMovieDto for cast/crew entries - TMDB adds extra fields here
// (character, job, credit_id) that TmdbMovieDto's @JsonIgnoreProperties
// already discards harmlessly, and we only need id/title/poster/etc. to
// build a candidate pool from a person's filmography.
@JsonIgnoreProperties(ignoreUnknown = true)
public record TmdbPersonMovieCreditsDto(
        List<TmdbMovieDto> cast,
        List<TmdbMovieDto> crew
) {}