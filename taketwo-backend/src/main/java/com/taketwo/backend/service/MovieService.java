package com.taketwo.backend.service;

import com.taketwo.backend.dto.MovieDetailResponse;
import com.taketwo.backend.dto.MovieSummaryResponse;
import com.taketwo.backend.tmdb.TmdbClient;
import com.taketwo.backend.tmdb.dto.TmdbMovieDto;
import com.taketwo.backend.tmdb.dto.TmdbPagedResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MovieService {

    private final TmdbClient tmdbClient;
    private final MovieCacheService movieCacheService;
    private final MovieMapper movieMapper;

    public List<MovieSummaryResponse> getPopular(int page) {
        return tmdbClient.getPopularMovies(page).results().stream()
                .map(movieMapper::toSummary)
                .toList();
    }

    public List<MovieSummaryResponse> getTrending(String timeWindow) {
        return tmdbClient.getTrendingMovies(timeWindow).results().stream()
                .map(movieMapper::toSummary)
                .toList();
    }

    public List<MovieSummaryResponse> search(String query, int page) {
        return tmdbClient.searchMovies(query, page).results().stream()
                .map(movieMapper::toSummary)
                .toList();
    }

    // Updated to accept 'int page' so your frontend can fetch page 2, 3, etc., as the user scrolls
    public List<MovieSummaryResponse> discoverMovies(String withGenres, String releaseYear, String sortBy, int page) {
        TmdbPagedResponse<TmdbMovieDto> response = tmdbClient.discoverMovies(withGenres, releaseYear, sortBy, page);

        if (response != null && response.results() != null) {
            return response.results().stream()
                    .map(movieMapper::toSummary)
                    .toList();
        }

        return List.of();
    }

    public MovieDetailResponse getDetail(Long tmdbId) {
        var movie = movieCacheService.findOrCacheMovie(tmdbId);
        return movieMapper.toDetail(movie);
    }
}