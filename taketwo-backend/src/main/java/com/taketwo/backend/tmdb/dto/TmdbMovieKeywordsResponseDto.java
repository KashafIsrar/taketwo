// tmdb/dto/TmdbMovieKeywordsResponseDto.java
package com.taketwo.backend.tmdb.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

// TMDB's field is literally "keywords" for the movie-keywords endpoint
// (it's "results" on the TV equivalent - we only ever call the movie one).
@JsonIgnoreProperties(ignoreUnknown = true)
public record TmdbMovieKeywordsResponseDto(List<TmdbKeywordDto> keywords) {}