package com.taketwo.backend.controller;

import com.taketwo.backend.dto.MovieDetailResponse;
import com.taketwo.backend.dto.MovieSummaryResponse;
import com.taketwo.backend.service.MovieService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/movies")
@RequiredArgsConstructor
public class MovieController {

    private final MovieService movieService;

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

    // Viewing a detail page is one of the two cache-on-demand triggers.
    @GetMapping("/{tmdbId}")
    public MovieDetailResponse detail(@PathVariable Long tmdbId) {
        return movieService.getDetail(tmdbId);
    }
}
