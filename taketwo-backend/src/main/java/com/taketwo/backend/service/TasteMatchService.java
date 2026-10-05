package com.taketwo.backend.service;

import com.taketwo.backend.dto.*;
import com.taketwo.backend.dto.projection.MovieRatingProjection;
import com.taketwo.backend.entity.MovieLog;
import com.taketwo.backend.entity.User;
import com.taketwo.backend.repository.MovieLogRepository;
import com.taketwo.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@Service
@RequiredArgsConstructor
public class TasteMatchService {

    private static final int TOP_N_SHARED = 5;
    private static final int MOVIES_TO_SURFACE = 10;
    private static final double HIGH_RATING_THRESHOLD = 4.0;

    private final TasteProfileService tasteProfileService;
    private final MovieLogRepository movieLogRepository;
    private final MovieMapper movieMapper;
    private final UserRepository userRepository;

    public TasteMatchResponse getMatch(UUID currentUserId, UUID targetUserId) {
        if (currentUserId.equals(targetUserId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Can't match against yourself");
        }
        User target = userRepository.findById(targetUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        TasteProfileResponse yours = tasteProfileService.getTasteProfile(currentUserId);
        TasteProfileResponse theirs = tasteProfileService.getTasteProfile(targetUserId);

        List<SharedGenre> sharedGenres = computeSharedGenres(yours, theirs);
        List<SharedDirector> sharedDirectors = computeSharedDirectors(yours, theirs);

        Map<Long, Double> yourRatings = toRatingMap(movieLogRepository.findTmdbIdAndRatingByUserId(currentUserId));
        Map<Long, Double> theirRatings = toRatingMap(movieLogRepository.findTmdbIdAndRatingByUserId(targetUserId));

        Set<Long> commonTmdbIds = new HashSet<>(yourRatings.keySet());
        commonTmdbIds.retainAll(theirRatings.keySet());

        Double ratingAlignmentPercent = null;
        if (!commonTmdbIds.isEmpty()) {
            double totalDiff = commonTmdbIds.stream()
                    .mapToDouble(id -> Math.abs(yourRatings.get(id) - theirRatings.get(id)))
                    .sum();
            double avgDiff = totalDiff / commonTmdbIds.size(); // range 0 (identical) to 4.5 (max possible on a 0.5-5.0 scale)
            ratingAlignmentPercent = Math.max(0, 100 - (avgDiff * 25));
        }

        int matchPercent = computeOverallScore(sharedGenres, sharedDirectors, ratingAlignmentPercent, yours, theirs);

        List<MovieSummaryResponse> notYetSeen = movieLogRepository
                .findHighlyRatedLogsByUserId(targetUserId, HIGH_RATING_THRESHOLD, PageRequest.of(0, MOVIES_TO_SURFACE * 2))
                .stream()
                .filter(log -> !yourRatings.containsKey(log.getMovie().getTmdbId()))
                .limit(MOVIES_TO_SURFACE)
                .map(log -> movieMapper.toSummary(log.getMovie()))
                .toList();

        return new TasteMatchResponse(
                targetUserId, target.getUsername(), matchPercent,
                sharedGenres, sharedDirectors, commonTmdbIds.size(),
                ratingAlignmentPercent, notYetSeen
        );
    }

    private List<SharedGenre> computeSharedGenres(TasteProfileResponse yours, TasteProfileResponse theirs) {
        Map<Integer, GenreAffinity> theirGenresById = toGenreMap(theirs.favoredGenres());
        return yours.favoredGenres().stream()
                .filter(g -> g.tmdbGenreId() != null && theirGenresById.containsKey(g.tmdbGenreId()))
                .map(g -> new SharedGenre(g.genreName(), g.filmCount(), theirGenresById.get(g.tmdbGenreId()).filmCount()))
                .sorted(Comparator.comparingLong((SharedGenre s) -> s.yourCount() + s.theirCount()).reversed())
                .limit(TOP_N_SHARED)
                .toList();
    }

    private List<SharedDirector> computeSharedDirectors(TasteProfileResponse yours, TasteProfileResponse theirs) {
        Map<UUID, DirectorAffinity> theirDirectorsById = new HashMap<>();
        theirs.topDirectors().forEach(d -> theirDirectorsById.put(d.personId(), d));

        return yours.topDirectors().stream()
                .filter(d -> theirDirectorsById.containsKey(d.personId()))
                .map(d -> new SharedDirector(d.name(), d.filmCount(), theirDirectorsById.get(d.personId()).filmCount()))
                .sorted(Comparator.comparingLong((SharedDirector s) -> s.yourCount() + s.theirCount()).reversed())
                .limit(TOP_N_SHARED)
                .toList();
    }

    // Weighted blend: rating alignment is the strongest signal when it
    // exists (you've both actually watched the same films), genre/director
    // overlap otherwise. Weights redistribute rather than silently scoring
    // "no shared movies" as "0% aligned" within a fixed-weight formula.
    private int computeOverallScore(List<SharedGenre> sharedGenres, List<SharedDirector> sharedDirectors,
                                     Double ratingAlignmentPercent, TasteProfileResponse yours, TasteProfileResponse theirs) {
        double genreScore = overlapScore(sharedGenres.size(), yours.favoredGenres().size(), theirs.favoredGenres().size());
        double directorScore = overlapScore(sharedDirectors.size(), yours.topDirectors().size(), theirs.topDirectors().size());

        double score;
        if (ratingAlignmentPercent != null) {
            score = (ratingAlignmentPercent * 0.5) + (genreScore * 0.3) + (directorScore * 0.2);
        } else {
            score = (genreScore * 0.65) + (directorScore * 0.35);
        }
        return (int) Math.round(Math.max(1, Math.min(99, score)));
    }

    private double overlapScore(int sharedCount, int yourTotal, int theirTotal) {
        int smallerPool = Math.min(Math.max(yourTotal, 1), Math.max(theirTotal, 1));
        return Math.min(100, (sharedCount * 100.0) / smallerPool);
    }

    private Map<Integer, GenreAffinity> toGenreMap(List<GenreAffinity> genres) {
        Map<Integer, GenreAffinity> map = new HashMap<>();
        genres.forEach(g -> { if (g.tmdbGenreId() != null) map.put(g.tmdbGenreId(), g); });
        return map;
    }

    private Map<Long, Double> toRatingMap(List<MovieRatingProjection> rows) {
        Map<Long, Double> map = new HashMap<>();
        rows.forEach(r -> map.put(r.tmdbId(), r.rating()));
        return map;
    }
}