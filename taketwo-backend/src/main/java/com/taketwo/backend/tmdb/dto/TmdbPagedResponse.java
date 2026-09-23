package com.taketwo.backend.tmdb.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TmdbPagedResponse<T>(
        int page,
        List<T> results,
        int total_pages,
        int total_results
) {}
