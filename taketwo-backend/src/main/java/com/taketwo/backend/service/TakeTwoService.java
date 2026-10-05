package com.taketwo.backend.service;

import com.taketwo.backend.dto.*;
import com.taketwo.backend.entity.Movie;
import com.taketwo.backend.repository.MovieLogRepository;
import com.taketwo.backend.tmdb.TmdbClient;
import com.taketwo.backend.tmdb.dto.TmdbMovieDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class TakeTwoService {

    // TMDB's standard genre id mapping - stable, well-known values, used here
    // instead of depending on whatever happens to be cached locally, since a
    // chosen mood/genre might not exist in our DB yet for a new user.
    private static final Map<String, Integer> MOOD_GENRE_MAP = Map.ofEntries(
            Map.entry("Action", 28), Map.entry("Comedy", 35), Map.entry("Drama", 18),
            Map.entry("Horror", 27), Map.entry("Romance", 10749), Map.entry("Science Fiction", 878),
            Map.entry("Thriller", 53), Map.entry("Mystery", 9648), Map.entry("Fantasy", 14),
            Map.entry("Animation", 16), Map.entry("Crime", 80), Map.entry("Documentary", 99)
    );

    private static final int POPULAR_MIN_VOTES = 1000;
    private static final int HIDDEN_GEM_MIN_VOTES = 50;
    private static final int HIDDEN_GEM_MAX_VOTES = 500;
    private static final int MAX_RUNTIME_CHECK_ATTEMPTS = 5;

    private final TasteProfileService tasteProfileService;
    private final TmdbClient tmdbClient;
    private final MovieLogRepository movieLogRepository;
    private final MovieCacheService movieCacheService;
    private final MovieMapper movieMapper;

    public TakeTwoResponse recommend(UUID userId, TakeTwoRequest request) {
        Integer genreId = MOOD_GENRE_MAP.get(request.genre());
        if (genreId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown genre: " + request.genre());
        }

        TasteProfileResponse taste = tasteProfileService.getTasteProfile(userId);
        Set<Long> alreadyLogged = movieLogRepository.findLoggedTmdbIdsByUserId(userId);
        String familiarity = request.familiarity() == null ? "EITHER" : request.familiarity();

        List<ScoredCandidate> candidates;
        boolean usedMomentum = false;

        // Momentum mode: if the user has a director kick AND didn't explicitly
        // ask for something new, prioritize that director's filmography -
        // this is what makes the "because you've watched 2 Nolan films this
        // month" case actually work, rather than generic genre matching.
        if (!taste.directorMomentum().isEmpty() && !"NEW".equals(familiarity)) {
            DirectorAffinity momentumDirector = taste.directorMomentum().get(0);
            candidates = candidatesFromDirector(momentumDirector, genreId, alreadyLogged);
            if (!candidates.isEmpty()) {
                usedMomentum = true;
            }
        } else {
            candidates = new ArrayList<>();
        }

        if (candidates.isEmpty()) {
            candidates = candidatesFromDiscover(genreId, request, taste, alreadyLogged);
        }

        if (candidates.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Couldn't find a match for that combination right now - try loosening the filters");
        }

        candidates.sort(Comparator.comparingInt(ScoredCandidate::score).reversed());

        // Pick randomly among the top-scoring candidates rather than always the
        // single highest score - keeps quality while varying between submissions.
        List<ScoredCandidate> topTier = candidates.stream().limit(Math.min(5, candidates.size())).toList();
        ScoredCandidate randomTopPick = topTier.get(java.util.concurrent.ThreadLocalRandom.current().nextInt(topTier.size()));

        List<ScoredCandidate> reordered = new ArrayList<>(topTier);
        reordered.remove(randomTopPick);
        reordered.add(0, randomTopPick);

        ScoredCandidate chosen = selectWithinRuntimeCap(reordered, request.maxRuntimeMinutes());
        boolean directorReason = usedMomentum;

        return buildResponse(chosen, taste, directorReason);
    }

    private record ScoredCandidate(TmdbMovieDto movie, int score, List<String> reasonTags) {}

    private List<ScoredCandidate> candidatesFromDirector(DirectorAffinity director, Integer genreId, Set<Long> alreadyLogged) {
        var filmography = tmdbClient.getPersonFilmography(director.tmdbPersonId());
        if (filmography == null || filmography.crew() == null) return new ArrayList<>();

        List<ScoredCandidate> genreMatched = new ArrayList<>();
        List<ScoredCandidate> allUnlogged = new ArrayList<>();

        for (var m : filmography.crew()) {
            if (alreadyLogged.contains(m.id())) {
                continue;
            }
            int score = 60; // base score for momentum-driven picks
            List<String> tags = new ArrayList<>();
            tags.add("Director momentum: " + director.name());
            
            boolean matchesGenre = m.genreIds() != null && m.genreIds().contains(genreId);
            if (matchesGenre) {
                score += 15;
                tags.add(reverseGenreLookup(genreId));
            }
            if (m.voteAverage() != null) {
                score += (int) Math.round(m.voteAverage()); // small tiebreaker
            }

            ScoredCandidate candidate = new ScoredCandidate(m, score, tags);
            allUnlogged.add(candidate);
            if (matchesGenre) {
                genreMatched.add(candidate);
            }
        }
        
        // Require genre match when the director has any films in that genre -
        // only fall back to their full filmography when they genuinely have none.
        return genreMatched.isEmpty() ? allUnlogged : genreMatched;
    }

    private List<ScoredCandidate> candidatesFromDiscover(Integer genreId, TakeTwoRequest request, TasteProfileResponse taste, Set<Long> alreadyLogged) {
        Integer minVotes = null, maxVotes = null;
        String popularity = request.popularity() == null ? "EITHER" : request.popularity();
        if ("POPULAR".equals(popularity)) {
            minVotes = POPULAR_MIN_VOTES;
        } else if ("HIDDEN_GEM".equals(popularity)) {
            minVotes = HIDDEN_GEM_MIN_VOTES;
            maxVotes = HIDDEN_GEM_MAX_VOTES;
        }

        // Randomize which page of TMDB's results we draw from (capped at 5)
        // so every submission doesn't return the exact same candidate pool.
        int randomPage = java.util.concurrent.ThreadLocalRandom.current().nextInt(1, 6);

        var page = tmdbClient.discoverMoviesForTakeTwo(
                String.valueOf(genreId), request.maxRuntimeMinutes(), minVotes, maxVotes, randomPage);

        if (page == null || page.results() == null || page.results().isEmpty()) {
            page = tmdbClient.discoverMoviesForTakeTwo(
                    String.valueOf(genreId), request.maxRuntimeMinutes(), minVotes, maxVotes, 1);
        }

        if (page == null || page.results() == null) return new ArrayList<>();

        Set<Integer> favoredGenreIds = taste.favoredGenres().stream()
                .map(GenreAffinity::tmdbGenreId).filter(Objects::nonNull).collect(java.util.stream.Collectors.toSet());

        List<ScoredCandidate> results = new ArrayList<>();
        for (var m : page.results()) {
            if (alreadyLogged.contains(m.id())) {
                continue;
            }
            int score = 30; // base score for a general discover pick
            List<String> tags = new ArrayList<>();
            tags.add(request.genre());

            long overlap = m.genreIds() == null ? 0 : m.genreIds().stream().filter(favoredGenreIds::contains).count();
            score += (int) (overlap * 20);
            if (overlap > 0) tags.add("Matches genres you rate highly");

            if (m.voteAverage() != null) {
                score += (int) Math.round(m.voteAverage());
            }
            results.add(new ScoredCandidate(m, score, tags));
        }
        return results;
    }

    private ScoredCandidate selectWithinRuntimeCap(List<ScoredCandidate> ranked, Integer maxRuntimeMinutes) {
        if (maxRuntimeMinutes == null) return ranked.get(0);

        int attempts = Math.min(MAX_RUNTIME_CHECK_ATTEMPTS, ranked.size());
        for (int i = 0; i < attempts; i++) {
            ScoredCandidate candidate = ranked.get(i);
            Movie cached = movieCacheService.findOrCacheMovie(candidate.movie().id());
            if (cached.getRuntimeMinutes() == null || cached.getRuntimeMinutes() <= maxRuntimeMinutes) {
                return candidate;
            }
        }
        return ranked.get(0); // none fit - return the top pick anyway rather than fail the whole request
    }

    private TakeTwoResponse buildResponse(ScoredCandidate chosen, TasteProfileResponse taste, boolean directorReason) {
        Movie movie = movieCacheService.findOrCacheMovie(chosen.movie().id());
        MovieSummaryResponse summary = movieMapper.toSummary(movie);

        String explanation = buildExplanation(chosen, taste, directorReason);
        int matchPercent = Math.max(1, Math.min(99, chosen.score()));

        return new TakeTwoResponse(summary, matchPercent, explanation, chosen.reasonTags());
    }

    private String buildExplanation(ScoredCandidate chosen, TasteProfileResponse taste, boolean directorReason) {
        if (directorReason && !taste.directorMomentum().isEmpty()) {
            DirectorAffinity d = taste.directorMomentum().get(0);
            return "You've logged " + d.filmCount() + " films by " + d.name()
                    + " in the last month, so here's another one.";
        }
        if (!taste.favoredGenres().isEmpty()) {
            GenreAffinity g = taste.favoredGenres().get(0);
            return "You rate " + g.genreName() + " films above your own average ("
                    + String.format("%.1f", g.averageRating()) + " vs your overall "
                    + String.format("%.1f", taste.overallAverageRating()) + "), so this one fits your taste.";
        }
        return "Picked based on what's popular right now in a genre you asked for.";
    }

    private String reverseGenreLookup(Integer genreId) {
        return MOOD_GENRE_MAP.entrySet().stream()
                .filter(e -> e.getValue().equals(genreId))
                .map(Map.Entry::getKey)
                .findFirst().orElse("");
    }
}