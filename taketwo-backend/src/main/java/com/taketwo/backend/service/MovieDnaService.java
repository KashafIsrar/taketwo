package com.taketwo.backend.service;

import com.taketwo.backend.dto.*;
import com.taketwo.backend.dto.projection.KeywordAffinityProjection;
import com.taketwo.backend.entity.Keyword;
import com.taketwo.backend.entity.Movie;
import com.taketwo.backend.repository.MovieLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MovieDnaService {

    private final VibeTagCatalog catalog;
    private final MovieCacheService movieCacheService;
    private final MovieLogRepository movieLogRepository;

    public MovieDnaResponse getMovieDna(Long tmdbId, UUID currentUserId) {
        Movie movie = movieCacheService.findOrCacheMovie(tmdbId);
        Set<String> rawKeywords = movie.getKeywords().stream().map(Keyword::getName).collect(Collectors.toSet());

        List<VibeTagCatalog.VibeTagDefinition> matched = catalog.resolveTags(rawKeywords);

        double pacing = 0, tone = 0, complexity = 0;
        for (var tag : matched) {
            pacing += tag.pacingWeight();
            tone += tag.toneWeight();
            complexity += tag.complexityWeight();
        }
        
        int n = Math.max(1, matched.size());

        AxisResult pacingResult = toAxisResult(pacing / n, "Slow", "Balanced", "Fast");
        AxisResult toneResult = toAxisResult(tone / n, "Dark", "Balanced", "Light");
        AxisResult complexityResult = toAxisResult(complexity / n, "Simple", "Balanced", "Intricate");

        List<String> vibeTags = matched.stream().map(VibeTagCatalog.VibeTagDefinition::displayName).toList();

        Integer matchPercent = currentUserId == null ? null : computeMatchPercent(currentUserId, vibeTags);

        return new MovieDnaResponse(movie.getId(), movie.getTmdbId(), vibeTags, pacingResult, toneResult, complexityResult, matchPercent);
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