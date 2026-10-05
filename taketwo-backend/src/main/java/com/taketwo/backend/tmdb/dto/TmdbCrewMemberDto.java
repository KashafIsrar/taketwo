// tmdb/dto/TmdbCrewMemberDto.java
package com.taketwo.backend.tmdb.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TmdbCrewMemberDto(
        Long id,
        String name,
        @JsonProperty("profile_path") String profilePath,
        String job
) {}