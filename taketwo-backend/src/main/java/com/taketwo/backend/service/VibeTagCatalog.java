// service/VibeTagCatalog.java
package com.taketwo.backend.service;

import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class VibeTagCatalog {

    // pacingWeight/toneWeight/complexityWeight each range roughly -1.0 to
    // 1.0. Pacing: negative = slower, positive = faster. Tone: negative =
    // darker, positive = lighter. Complexity: negative = simple/linear,
    // positive = intricate. A weight of 0 means that tag doesn't speak to
    // that particular axis. This is a deliberately simple, inspectable
    // heuristic - not a scientific measure of anything.
    public record VibeTagDefinition(
            String displayName, Set<String> matchingKeywords,
            double pacingWeight, double toneWeight, double complexityWeight
    ) {}

    private final List<VibeTagDefinition> catalog = List.of(
            def("Mind-Bending", Set.of("mind-bending", "nonlinear timeline", "alternate reality"), 0, -0.2, 1.0),
            def("Slow Burn", Set.of("slow burn", "slow paced"), -1.0, 0, 0.3),
            def("Dark", Set.of("dark comedy", "psychological", "noir", "neo-noir", "violence"), 0, -0.9, 0.2),
            def("Heist", Set.of("heist"), 0.7, -0.1, 0.4),
            def("Time Travel", Set.of("time travel"), 0, 0, 0.6),
            def("Dystopia", Set.of("dystopia", "post-apocalyptic"), -0.1, -0.6, 0.3),
            def("Coming Of Age", Set.of("coming of age"), -0.2, 0.3, 0),
            def("Feel-Good", Set.of("feel good"), 0.2, 1.0, -0.3),
            def("Action-Packed", Set.of("chase", "car chase", "explosion"), 1.0, 0, -0.2),
            def("Character-Driven", Set.of("character study"), -0.3, 0, 0.2),
            def("Twist Ending", Set.of("plot twist", "twist ending"), 0, 0, 0.5),
            def("Romance", Set.of("romance"), -0.1, 0.4, -0.1),
            def("Neo-Noir", Set.of("film noir"), -0.2, -0.8, 0.3),
            def("Survival", Set.of("survival"), 0.5, -0.4, 0),
            def("Satire", Set.of("satire"), 0.1, -0.2, 0.3),
            def("Epic", Set.of("epic"), -0.2, 0, 0.3),
            def("Supernatural", Set.of("supernatural"), 0, -0.3, 0.2),
            def("Found Footage", Set.of("found footage"), 0.4, -0.5, 0.1)
    );
    private final List<VibeTagDefinition> genreCatalog = List.of(
        def("Action", Set.of("action"), 0.8, -0.1, -0.2),
        def("Adventure", Set.of("adventure"), 0.5, 0.3, -0.1),
        def("Animated", Set.of("animation"), 0.1, 0.4, -0.1),
        def("Comedic", Set.of("comedy"), 0.3, 0.8, -0.3),
        def("Crime", Set.of("crime"), 0.1, -0.5, 0.3),
        def("Documentary", Set.of("documentary"), -0.3, -0.1, 0.2),
        def("Dramatic", Set.of("drama"), -0.4, -0.2, 0.2),
        def("Family", Set.of("family"), 0.2, 0.8, -0.4),
        def("Fantastical", Set.of("fantasy"), 0.1, 0.2, 0.3),
        def("Historical", Set.of("history"), -0.4, -0.2, 0.3),
        def("Horror", Set.of("horror"), 0.2, -0.9, 0.0),
        def("Musical", Set.of("music"), 0.2, 0.5, -0.2),
        def("Mysterious", Set.of("mystery"), -0.1, -0.4, 0.6),
        def("Romantic", Set.of("romance"), -0.2, 0.5, -0.2),
        def("Sci-Fi", Set.of("science fiction"), 0.1, -0.1, 0.5),
        def("Suspenseful", Set.of("thriller"), 0.5, -0.6, 0.4),
        def("War", Set.of("war"), 0.2, -0.7, 0.3),
        def("Western", Set.of("western"), -0.1, -0.2, 0.0)
);

// Coarser than keyword matching, but every cached film has genres, so this
// guarantees most films get a real profile instead of nothing.
public List<VibeTagDefinition> resolveGenreTags(Set<String> genreNames) {
    Set<String> lower = genreNames.stream().map(String::toLowerCase).collect(java.util.stream.Collectors.toSet());
    return genreCatalog.stream()
            .filter(def -> def.matchingKeywords().stream().anyMatch(lower::contains))
            .toList();
}


    private VibeTagDefinition def(String name, Set<String> keywords, double pacing, double tone, double complexity) {
        Set<String> normalized = keywords.stream().map(String::toLowerCase).collect(java.util.stream.Collectors.toSet());
        return new VibeTagDefinition(name, normalized, pacing, tone, complexity);
    }

    // Given a movie's raw TMDB keyword names, return the catalog entries that matched.
    public List<VibeTagDefinition> resolveTags(Set<String> rawKeywordNames) {
        Set<String> lowerRaw = rawKeywordNames.stream().map(String::toLowerCase).collect(java.util.stream.Collectors.toSet());
        return catalog.stream()
                .filter(def -> def.matchingKeywords().stream().anyMatch(lowerRaw::contains))
                .toList();
    }

    public List<VibeTagDefinition> all() {
        return catalog;
    }
}