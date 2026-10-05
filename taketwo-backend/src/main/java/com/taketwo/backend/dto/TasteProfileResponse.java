// dto/TasteProfileResponse.java
package com.taketwo.backend.dto;

import java.util.List;
import java.util.UUID;

public record TasteProfileResponse(
        UUID userId,
        long totalLogged,
        Double overallAverageRating,
        List<GenreAffinity> topGenres,       // highest film count
        List<GenreAffinity> favoredGenres,   // rated above the user's own overall average - the real preference signal
        List<DecadeAffinity> decadeAffinity,
        List<DirectorAffinity> topDirectors,
        List<DirectorAffinity> directorMomentum  // 2+ films by the same director in the last 30 days
) {}