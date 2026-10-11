package com.taketwo.backend.service;

import com.taketwo.backend.dto.AxisResult;
import com.taketwo.backend.dto.MovieDnaResponse;
import com.taketwo.backend.dto.projection.KeywordAffinityProjection;
import com.taketwo.backend.entity.Genre;
import com.taketwo.backend.entity.Keyword;
import com.taketwo.backend.entity.Movie;
import com.taketwo.backend.repository.MovieLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MovieDnaService {

    private final VibeTagCatalog catalog;
    private final MovieCacheService movieCacheService;
    private final MovieLogRepository movieLogRepository;

    private record ResolvedTags(List<VibeTagCatalog.VibeTagDefinition> tags, String source) {}

    // Transactional so the lazy keywords/genres collections are reliably
    // readable here instead of depending on open-session-in-view.
    @Transactional
    public MovieDnaResponse getMovieDna(Long tmdbId, UUID currentUserId) {
        Movie movie = movieCacheService.findOrCacheMovie(tmdbId);
        ResolvedTags resolved = resolveTags(movie);
        List<VibeTagCatalog.VibeTagDefinition> matched = resolved.tags();

        double pacing = 0, tone = 0, complexity = 0;
        for (var tag : matched) {
            pacing += tag.pacingWeight();
            tone += tag.toneWeight();
            complexity += tag.complexityWeight();
        }
        int n = Math.max(1, matched.size());   // zero tags yields a neutral "Balanced" profile

        AxisResult pacingResult = toAxisResult(pacing / n, "Slow", "Balanced", "Fast");
        AxisResult toneResult = toAxisResult(tone / n, "Dark", "Balanced", "Light");
        AxisResult complexityResult = toAxisResult(complexity / n, "Simple", "Balanced", "Intricate");

        List<String> vibeTags = matched.stream().map(VibeTagCatalog.VibeTagDefinition::displayName).toList();

        // The taste-match comparison is built from keyword-derived tags, so
        // it's only meaningful for KEYWORDS profiles. Showing a percentage
        // computed from genre-only tags would be a made-up number.
        Integer matchPercent = (currentUserId != null && "KEYWORDS".equals(resolved.source()))
                ? computeMatchPercent(currentUserId, vibeTags)
                : null;

        return new MovieDnaResponse(
                movie.getId(), movie.getTmdbId(), vibeTags,
                pacingResult, toneResult, complexityResult, matchPercent, resolved.source()
        );
    }

    private ResolvedTags resolveTags(Movie movie) {
        Set<String> rawKeywords = movie.getKeywords().stream().map(Keyword::getName).collect(Collectors.toSet());
        var keywordTags = catalog.resolveTags(rawKeywords);
        if (!keywordTags.isEmpty()) return new ResolvedTags(keywordTags, "KEYWORDS");

        Set<String> genreNames = movie.getGenres().stream().map(Genre::getName).collect(Collectors.toSet());
        var genreTags = catalog.resolveGenreTags(genreNames);
        if (!genreTags.isEmpty()) return new ResolvedTags(genreTags, "GENRE");

        return new ResolvedTags(List.of(), "NONE");
    }

    private AxisResult toAxisResult(double score, String negLabel, String neutralLabel, String posLabel) {
        String label = score < -0.2 ? negLabel : score > 0.2 ? posLabel : neutralLabel;
        return new AxisResult(Math.round(score * 100.0) / 100.0, label);
    }

    private Integer computeMatchPercent(UUID userId, List<String> movieVibeTags) {
        if (movieVibeTags.isEmpty()) return null;

        List<KeywordAffinityProjection> rawAffinity = movieLogRepository.findKeywordAffinityByUserId(userId);
        if (rawAffinity.isEmpty()) return null;

        Map<String, List<KeywordAffinityProjection>> rawByKeyword = rawAffinity.stream()
                .collect(Collectors.groupingBy(r -> r.keywordName().toLowerCase()));

        double overallAvgRating = rawAffinity.stream()
                .filter(r -> r.averageRating() != null)
                .mapToDouble(KeywordAffinityProjection::averageRating)
                .average().orElse(0);

        Set<String> userFavoredTags = new HashSet<>();
        for (var def : catalog.all()) {
            boolean favored = def.matchingKeywords().stream()
                    .flatMap(kw -> rawByKeyword.getOrDefault(kw, List.of()).stream())
                    .anyMatch(r -> r.averageRating() != null && r.averageRating() > overallAvgRating);
            if (favored) userFavoredTags.add(def.displayName());
        }

        if (userFavoredTags.isEmpty()) return null;

        long overlap = movieVibeTags.stream().filter(userFavoredTags::contains).count();
        double score = (overlap * 100.0) / movieVibeTags.size();
        return (int) Math.round(Math.max(1, Math.min(99, score)));
    }
}