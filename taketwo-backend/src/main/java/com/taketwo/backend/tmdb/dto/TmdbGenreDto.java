package com.taketwo.backend.tmdb.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TmdbGenreDto(
        Integer id,
        String name
) {}
