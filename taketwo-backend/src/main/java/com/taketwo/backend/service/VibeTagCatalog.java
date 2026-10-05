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