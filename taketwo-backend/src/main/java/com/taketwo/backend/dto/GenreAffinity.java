// dto/GenreAffinity.java
package com.taketwo.backend.dto;

public record GenreAffinity(
        Integer tmdbGenreId,
        String genreName,
        long filmCount,
        Double averageRating
) {}