// tmdb/dto/TmdbCreditsDto.java
package com.taketwo.backend.tmdb.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TmdbCreditsDto(
        List<TmdbCastMemberDto> cast,
        List<TmdbCrewMemberDto> crew
) {}