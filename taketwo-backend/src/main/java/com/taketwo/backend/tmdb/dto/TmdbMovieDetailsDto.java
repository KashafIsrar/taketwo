package com.taketwo.backend.tmdb.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TmdbMovieDetailsDto(
        Long id,
        String title,
        String overview,
        @JsonProperty("poster_path") String posterPath,
        @JsonProperty("release_date") String releaseDate,
        @JsonProperty("vote_average") Double voteAverage,
        Integer runtime,
        List<TmdbGenreDto> genres
) {}
