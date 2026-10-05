package com.taketwo.backend.tmdb;

import com.taketwo.backend.tmdb.dto.TmdbCreditsDto;
import com.taketwo.backend.tmdb.dto.TmdbMovieDetailsDto;
import com.taketwo.backend.tmdb.dto.TmdbMovieDto;
import com.taketwo.backend.tmdb.dto.TmdbMovieKeywordsResponseDto;
import com.taketwo.backend.tmdb.dto.TmdbPagedResponse;
import com.taketwo.backend.tmdb.dto.TmdbPersonMovieCreditsDto;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

@Component
public class TmdbClient {

    private final RestClient restClient;
    private final String apiKey;

    public TmdbClient(
            @Value("${taketwo.tmdb.base-url}") String baseUrl,
            @Value("${taketwo.tmdb.api-key}") String apiKey
    ) {
        this.apiKey = apiKey;
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    public TmdbPagedResponse<TmdbMovieDto> getPopularMovies(int page) {
        return restClient.get()
                .uri(uriBuilder -> uriBuilder.path("/movie/popular")
                        .queryParam("api_key", apiKey)
                        .queryParam("page", page)
                        .build())
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }

    // TMDB's "time_window" is either "day" or "week"
    public TmdbPagedResponse<TmdbMovieDto> getTrendingMovies(String timeWindow) {
        String window = "day".equals(timeWindow) ? "day" : "week";
        return restClient.get()
                .uri(uriBuilder -> uriBuilder.path("/trending/movie/{window}")
                        .queryParam("api_key", apiKey)
                        .build(window))
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }

    public TmdbPagedResponse<TmdbMovieDto> searchMovies(String query, int page) {
        return restClient.get()
                .uri(uriBuilder -> uriBuilder.path("/search/movie")
                        .queryParam("api_key", apiKey)
                        .queryParam("query", query)
                        .queryParam("page", page)
                        .build())
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }

    /**
     * DISCOVER METHOD ADDED: Queries TMDB's official discover endpoint
     * so that genres, release years, and sort orders return completely unique items.
     */
    public TmdbPagedResponse<TmdbMovieDto> discoverMovies(String withGenres, String releaseYear, String sortBy, int page) {
        return restClient.get()
                .uri(uriBuilder -> {
                    uriBuilder.path("/discover/movie")
                            .queryParam("api_key", apiKey)
                            .queryParam("page", page);
                    
                    if (withGenres != null && !withGenres.isEmpty()) {
                        uriBuilder.queryParam("with_genres", withGenres);
                    }
                    if (releaseYear != null && !releaseYear.isEmpty()) {
                        uriBuilder.queryParam("primary_release_year", releaseYear);
                    }
                    if (sortBy != null && !sortBy.isEmpty()) {
                        uriBuilder.queryParam("sort_by", sortBy);
                    }
                    
                    return uriBuilder.build();
                })
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }

    /**
     * Returns null if TMDB doesn't have a movie with this id, rather than
     * throwing - callers (MovieCacheService) turn that into a clean 404.
     */
    public TmdbMovieDetailsDto getMovieDetails(Long tmdbId) {
        try {
            return restClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/movie/{id}")
                            .queryParam("api_key", apiKey)
                            .build(tmdbId))
                    .retrieve()
                    .body(TmdbMovieDetailsDto.class);
        } catch (HttpClientErrorException.NotFound ex) {
            return null;
        }
    }
    // Add to TmdbClient.java

public TmdbCreditsDto getMovieCredits(Long tmdbId) {
    try {
        return restClient.get()
                .uri(uriBuilder -> uriBuilder.path("/movie/{id}/credits")
                        .queryParam("api_key", apiKey)
                        .build(tmdbId))
                .retrieve()
                .body(TmdbCreditsDto.class);
    } catch (HttpClientErrorException.NotFound ex) {
        return null;
    }
}

public TmdbPersonMovieCreditsDto getPersonFilmography(Long tmdbPersonId) {
    try {
        return restClient.get()
                .uri(uriBuilder -> uriBuilder.path("/person/{id}/movie_credits")
                        .queryParam("api_key", apiKey)
                        .build(tmdbPersonId))
                .retrieve()
                .body(TmdbPersonMovieCreditsDto.class);
    } catch (HttpClientErrorException.NotFound ex) {
        return null;
    }
}

// New overload, separate from the existing 4-arg discoverMovies used by the
// Discover page's genre chips - TAKE TWO additionally needs runtime cap and
// a minimum vote count (the actual mechanism behind "hidden gem" filtering,
// since our local Movie cache never stored vote_count).
public TmdbPagedResponse<TmdbMovieDto> discoverMoviesForTakeTwo(
        String withGenres, Integer maxRuntimeMinutes, Integer minVoteCount, Integer maxVoteCount, int page
) {
    return restClient.get()
            .uri(uriBuilder -> {
                uriBuilder.path("/discover/movie")
                        .queryParam("api_key", apiKey)
                        .queryParam("page", page)
                        .queryParam("sort_by", "popularity.desc");

                if (withGenres != null && !withGenres.isEmpty()) {
                    uriBuilder.queryParam("with_genres", withGenres);
                }
                if (maxRuntimeMinutes != null) {
                    uriBuilder.queryParam("with_runtime.lte", maxRuntimeMinutes);
                }
                if (minVoteCount != null) {
                    uriBuilder.queryParam("vote_count.gte", minVoteCount);
                }
                if (maxVoteCount != null) {
                    uriBuilder.queryParam("vote_count.lte", maxVoteCount);
                }
                return uriBuilder.build();
            })
            .retrieve()
            .body(new ParameterizedTypeReference<>() {});
}

// Add to TmdbClient.java

public TmdbMovieKeywordsResponseDto getMovieKeywords(Long tmdbId) {
    try {
        return restClient.get()
                .uri(uriBuilder -> uriBuilder.path("/movie/{id}/keywords")
                        .queryParam("api_key", apiKey)
                        .build(tmdbId))
                .retrieve()
                .body(TmdbMovieKeywordsResponseDto.class);
    } catch (HttpClientErrorException.NotFound ex) {
        return null;
    }
}
}
