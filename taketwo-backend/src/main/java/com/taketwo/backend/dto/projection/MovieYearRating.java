// dto/projection/MovieYearRating.java - internal JPQL projection, not an API response shape
package com.taketwo.backend.dto.projection;

import java.time.LocalDate;

public record MovieYearRating(LocalDate releaseDate, Double rating) {}