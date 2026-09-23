package com.taketwo.backend.service;

import com.taketwo.backend.dto.MovieDetailResponse;
import com.taketwo.backend.dto.MovieSummaryResponse;
import com.taketwo.backend.entity.Genre;
import com.taketwo.backend.entity.Movie;
import com.taketwo.backend.tmdb.dto.TmdbMovieDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

@Component
public class MovieMapper {

    private final String imageBaseUrl;

    public MovieMapper(@Value("${taketwo.tmdb.image-base-url}") String imageBaseUrl) {
        this.imageBaseUrl = imageBaseUrl;
    }

    public MovieSummaryResponse toSummary(Movie movie) {
        return new MovieSummaryResponse(
                movie.getId(),
                movie.getTmdbId(),
                movie.getTitle(),
                posterUrl(movie.getPosterPath()),
                movie.getReleaseDate(),
                movie.getVoteAverage()
        );
    }

    // For browse results (popular/trending/search) that haven't been cached yet - id is null
    public MovieSummaryResponse toSummary(TmdbMovieDto dto) {
        return new MovieSummaryResponse(
                null,
                dto.id(),
                dto.title(),
                posterUrl(dto.posterPath()),
                parseDate(dto.releaseDate()),
                dto.voteAverage()
        );
    }

    public MovieDetailResponse toDetail(Movie movie) {
        List<String> genreNames = movie.getGenres().stream()
                .map(Genre::getName)
                .sorted()
                .toList();

        return new MovieDetailResponse(
                movie.getId(),
                movie.getTmdbId(),
                movie.getTitle(),
                movie.getOverview(),
                posterUrl(movie.getPosterPath()),
                movie.getReleaseDate(),
                movie.getRuntimeMinutes(),
                movie.getVoteAverage(),
                genreNames
        );
    }

    public String posterUrl(String posterPath) {
        if (posterPath == null || posterPath.isBlank()) {
            return null;
        }
        return imageBaseUrl + "/w500" + posterPath;
    }

    public LocalDate parseDate(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(raw);
        } catch (DateTimeParseException ex) {
            return null;
        }
    }
}
