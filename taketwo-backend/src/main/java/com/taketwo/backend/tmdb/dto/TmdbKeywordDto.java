// tmdb/dto/TmdbKeywordDto.java
package com.taketwo.backend.tmdb.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TmdbKeywordDto(Integer id, String name) {}