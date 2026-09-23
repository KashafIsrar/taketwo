package com.taketwo.backend.service;

import com.taketwo.backend.entity.Genre;
import com.taketwo.backend.entity.Movie;
import com.taketwo.backend.repository.GenreRepository;
import com.taketwo.backend.repository.MovieRepository;
import com.taketwo.backend.tmdb.TmdbClient;
import com.taketwo.backend.tmdb.dto.TmdbGenreDto;
import com.taketwo.backend.tmdb.dto.TmdbMovieDetailsDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MovieCacheService {

    private final MovieRepository movieRepository;
    private final GenreRepository genreRepository;
    private final TmdbClient tmdbClient;
    private final MovieMapper movieMapper;

    /**
     * The core of "cache-on-demand": returns the locally persisted Movie for
     * this tmdbId, fetching + saving it from TMDB the first time anyone
     * views or logs it. Every subsequent call is a plain DB read.
     */
    @Transactional
    public Movie findOrCacheMovie(Long tmdbId) {
        return movieRepository.findByTmdbId(tmdbId)
                .orElseGet(() -> cacheFromTmdb(tmdbId));
    }

    private Movie cacheFromTmdb(Long tmdbId) {
        TmdbMovieDetailsDto details = tmdbClient.getMovieDetails(tmdbId);
        if (details == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No TMDB movie with id " + tmdbId);
        }

        Set<Genre> genres = details.genres() == null
                ? Set.of()
                : details.genres().stream().map(this::resolveGenre).collect(Collectors.toSet());

        Movie movie = Movie.builder()
                .tmdbId(details.id())
                .title(details.title())
                .overview(details.overview())
                .posterPath(details.posterPath())
                .releaseDate(movieMapper.parseDate(details.releaseDate()))
                .runtimeMinutes(details.runtime())
                .voteAverage(details.voteAverage())
                .genres(genres)
                .syncedAt(Instant.now())
                .build();

        return movieRepository.save(movie);
    }

    // Genres are shared across movies, so we look up by TMDB's genre id
    // before creating a new row - avoids duplicate "Thriller" genres.
    private Genre resolveGenre(TmdbGenreDto dto) {
        return genreRepository.findByTmdbGenreId(dto.id())
                .orElseGet(() -> genreRepository.save(
                        Genre.builder().tmdbGenreId(dto.id()).name(dto.name()).build()));
    }
}
