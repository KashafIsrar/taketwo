
package com.taketwo.backend.service;

import com.taketwo.backend.dto.*;
import com.taketwo.backend.dto.projection.MovieYearRating;
import com.taketwo.backend.repository.MovieCreditRepository;
import com.taketwo.backend.repository.MovieLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.Set;
import java.util.Map;
import java.util.List;

import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class TasteProfileService {

    private static final int RECENCY_WINDOW_DAYS = 30;
    private static final int TOP_N = 5;

    private final MovieLogRepository movieLogRepository;
    private final MovieCreditRepository movieCreditRepository;

    public TasteProfileResponse getTasteProfile(UUID userId) {
        long totalLogged = movieLogRepository.countByUser_Id(userId);
        Double overallAvg = totalLogged > 0 ? movieLogRepository.findAverageRatingByUserId(userId) : null;

        List<GenreAffinity> allGenres = movieLogRepository.findGenreAffinityByUserId(userId);
        List<GenreAffinity> topGenres = allGenres.stream().limit(TOP_N).toList();

        // The real preference signal per the product philosophy: not just
        // "watched a lot of X" but "rates X noticeably higher than their own
        // baseline" - this is what the explanation text should lean on.
        List<GenreAffinity> favoredGenres = overallAvg == null ? List.of() : allGenres.stream()
                .filter(g -> g.averageRating() != null && g.averageRating() > overallAvg)
                .sorted(Comparator.comparing(GenreAffinity::averageRating).reversed())
                .toList();

        List<DecadeAffinity> decadeAffinity = computeDecadeAffinity(
                movieLogRepository.findReleaseDateAndRatingByUserId(userId));

        List<DirectorAffinity> topDirectors = movieCreditRepository
                .findDirectorAffinityByUserId(userId).stream().limit(TOP_N).toList();

        LocalDate recencyFrom = LocalDate.now().minusDays(RECENCY_WINDOW_DAYS);
        List<DirectorAffinity> directorMomentum = movieCreditRepository
                .findDirectorMomentumByUserId(userId, recencyFrom);

        return new TasteProfileResponse(
                userId, totalLogged, overallAvg, topGenres, favoredGenres,
                decadeAffinity, topDirectors, directorMomentum
        );
    }

    private List<DecadeAffinity> computeDecadeAffinity(List<MovieYearRating> rows) {
        Map<Integer, List<Double>> ratingsByDecade = new HashMap<>();
        Map<Integer, Long> countsByDecade = new HashMap<>();

        for (MovieYearRating row : rows) {
            int decade = (row.releaseDate().getYear() / 10) * 10;
            countsByDecade.merge(decade, 1L, Long::sum);
            if (row.rating() != null) {
                ratingsByDecade.computeIfAbsent(decade, k -> new ArrayList<>()).add(row.rating());
            }
        }

        return countsByDecade.entrySet().stream()
                .map(e -> {
                    List<Double> ratings = ratingsByDecade.getOrDefault(e.getKey(), List.of());
                    Double avg = ratings.isEmpty() ? null
                            : ratings.stream().mapToDouble(Double::doubleValue).average().orElse(0);
                    return new DecadeAffinity(e.getKey(), e.getValue(), avg);
                })
                .sorted(Comparator.comparingLong(DecadeAffinity::filmCount).reversed())
                .toList();
    }
}