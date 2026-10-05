package com.taketwo.backend.controller;

import com.taketwo.backend.dto.MovieDetailResponse;
import com.taketwo.backend.dto.MovieDnaResponse;
import com.taketwo.backend.dto.MovieSummaryResponse;
import com.taketwo.backend.service.CurrentUserService;
import com.taketwo.backend.service.MovieDnaService;
import com.taketwo.backend.service.MovieService;
import lombok.RequiredArgsConstructor;

import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/movies")
@RequiredArgsConstructor
public class MovieController {

    private final MovieService movieService;
    private final MovieDnaService movieDnaService;
    private final CurrentUserService currentUserService;

    @GetMapping("/popular")
    public List<MovieSummaryResponse> popular(@RequestParam(defaultValue = "1") int page) {
        return movieService.getPopular(page);
    }

    @GetMapping("/trending")
    public List<MovieSummaryResponse> trending(@RequestParam(defaultValue = "week") String window) {
        return movieService.getTrending(window);
    }

    @GetMapping("/search")
    public List<MovieSummaryResponse> search(
            @RequestParam String q,
            @RequestParam(defaultValue = "1") int page
    ) {
        return movieService.search(q, page);
    }

    // Updated discover endpoint now accepts an optional 'page' parameter for infinite scrolling/pagination
    @GetMapping("/discover")
    public List<MovieSummaryResponse> discover(
            @RequestParam(required = false) String with_genres,
            @RequestParam(required = false) String primary_release_year,
            @RequestParam(required = false) String sort_by,
            @RequestParam(defaultValue = "1") int page
    ) {
        return movieService.discoverMovies(with_genres, primary_release_year, sort_by, page);
    }

    @GetMapping("/{tmdbId}")
    public MovieDetailResponse detail(@PathVariable Long tmdbId) {
        return movieService.getDetail(tmdbId);
    }

    @GetMapping("/{tmdbId}/dna")
    public MovieDnaResponse getMovieDna(@PathVariable Long tmdbId, Authentication authentication) {
        UUID currentUserId = (authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken))
                ? currentUserService.getCurrentUser(authentication).getId()
                : null;
        return movieDnaService.getMovieDna(tmdbId, currentUserId);
    }
}