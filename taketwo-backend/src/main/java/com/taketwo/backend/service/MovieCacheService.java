package com.taketwo.backend.service;

import com.taketwo.backend.entity.Genre;
import com.taketwo.backend.entity.Movie;
import com.taketwo.backend.entity.MovieCredit;
import com.taketwo.backend.entity.Person;
import com.taketwo.backend.repository.GenreRepository;
import com.taketwo.backend.repository.MovieCreditRepository;
import com.taketwo.backend.repository.MovieRepository;
import com.taketwo.backend.repository.PersonRepository;
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
    private final PersonRepository personRepository;
    private final MovieCreditRepository movieCreditRepository;

    /**
     * The core of "cache-on-demand": returns the locally persisted Movie for
     * this tmdbId, fetching + saving it from TMDB the first time anyone
     * views or logs it. Every subsequent call is a plain DB read.
     * Also handles automatic credit backfilling for legacy cached items.
     */
    @Transactional
    public Movie findOrCacheMovie(Long tmdbId) {
        Movie movie = movieRepository.findByTmdbId(tmdbId)
                .orElseGet(() -> cacheFromTmdb(tmdbId));

        // Backfill credits for movies cached before this feature existed -
        // cache-on-demand still applies, it just also now covers credits.
        if (movieCreditRepository.countByMovie_Id(movie.getId()) == 0) {
            cacheCredits(movie);
        }

        return movie;
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

        Movie savedMovie = movieRepository.save(movie);
        
        // Immediately fetch and cache credits for newly cached movies
        cacheCredits(savedMovie);

        return savedMovie;
    }

    // Genres are shared across movies, so we look up by TMDB's genre id
    // before creating a new row - avoids duplicate "Thriller" genres.
    private Genre resolveGenre(TmdbGenreDto dto) {
        return genreRepository.findByTmdbGenreId(dto.id())
                .orElseGet(() -> genreRepository.save(
                        Genre.builder().tmdbGenreId(dto.id()).name(dto.name()).build()));
    }

    private void cacheCredits(Movie movie) {
        var credits = tmdbClient.getMovieCredits(movie.getTmdbId());
        if (credits == null) return;

        if (credits.crew() != null) {
            credits.crew().stream()
                    .filter(c -> "Director".equals(c.job()))
                    .forEach(director -> saveCredit(movie, director.id(), director.name(), director.profilePath(), MovieCredit.Role.DIRECTOR, null));
        }

        if (credits.cast() != null) {
            credits.cast().stream()
                    .filter(c -> c.order() != null && c.order() < 10) // top-billed only
                    .forEach(actor -> saveCredit(movie, actor.id(), actor.name(), actor.profilePath(), MovieCredit.Role.ACTOR, actor.order()));
        }
    }

    private void saveCredit(Movie movie, Long tmdbPersonId, String name, String profilePath, MovieCredit.Role role, Integer billingOrder) {
        Person person = personRepository.findByTmdbPersonId(tmdbPersonId)
                .orElseGet(() -> personRepository.save(
                        Person.builder().tmdbPersonId(tmdbPersonId).name(name).profilePath(profilePath).build()));

        // Ensure unique constraint safety across identical movie/person/role mappings
        boolean exists = movieCreditRepository.findByMovie_Id(movie.getId()).stream()
                .anyMatch(mc -> mc.getPerson().getId().equals(person.getId()) && mc.getRole() == role);

        if (!exists) {
            movieCreditRepository.save(
                    MovieCredit.builder().movie(movie).person(person).role(role).billingOrder(billingOrder).build());
        }
    }
}