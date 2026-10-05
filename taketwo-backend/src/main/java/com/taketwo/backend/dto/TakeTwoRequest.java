package com.taketwo.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

public record TakeTwoRequest(
        @NotBlank String genre,
        @JsonProperty("maxRuntimeMinutes") Integer maxRuntimeMinutes,
        @JsonProperty("familiarity") String familiarity,
        @JsonProperty("popularity") String popularity
) {
    public TakeTwoRequest {
        if (familiarity == null || familiarity.isBlank()) {
            familiarity = "EITHER";
        }
        if (popularity == null || popularity.isBlank()) {
            popularity = "EITHER";
        }
    }
}