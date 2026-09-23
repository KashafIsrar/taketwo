package com.taketwo.backend.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;

import java.time.LocalDate;

public record LogMovieRequest(

        @NotNull Long tmdbId,

        @NotNull
        @PastOrPresent(message = "watchedDate can't be in the future")
        LocalDate watchedDate,

        @NotNull
        @DecimalMin(value = "0.5", message = "rating must be at least 0.5")
        @DecimalMax(value = "5.0", message = "rating must be at most 5.0")
        Double rating,

        boolean rewatch,

        // Optional - keeping logging low-friction (quick star rating) vs writing a full review
        String reviewText,

        Boolean containsSpoilers
) {}
