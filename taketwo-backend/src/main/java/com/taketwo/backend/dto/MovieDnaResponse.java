// dto/MovieDnaResponse.java
package com.taketwo.backend.dto;

import java.util.List;
import java.util.UUID;

public record MovieDnaResponse(
        UUID movieId,
        Long tmdbId,
        List<String> vibeTags,
        AxisResult pacing,
        AxisResult tone,
        AxisResult complexity,
        Integer matchPercent,   // only computed for KEYWORDS-sourced profiles
        String source           // "KEYWORDS" | "GENRE" | "NONE"
) {}