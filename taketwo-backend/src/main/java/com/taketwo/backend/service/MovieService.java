package com.taketwo.backend.service;

import com.taketwo.backend.dto.MovieDetailResponse;
import com.taketwo.backend.dto.MovieSummaryResponse;
import com.taketwo.backend.tmdb.TmdbClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MovieService {

    private final TmdbClient tmdbClient;
    private final MovieCacheService movieCacheService;
    private final MovieMapper movieMapper;

    // Browsing endpoints deliberately do NOT persist anything - per the
    // cache-on-demand rule, a movie only enters our DB once someone views
    // its detail page or logs/watchlists it.
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

    public MovieDetailResponse getDetail(Long tmdbId) {
        var movie = movieCacheService.findOrCacheMovie(tmdbId);
        return movieMapper.toDetail(movie);
    }
}
