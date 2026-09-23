package com.taketwo.backend.service;

import com.taketwo.backend.dto.LogMovieRequest;
import com.taketwo.backend.dto.MovieLogResponse;
import com.taketwo.backend.entity.Movie;
import com.taketwo.backend.entity.MovieLog;
import com.taketwo.backend.entity.Review;
import com.taketwo.backend.entity.User;
import com.taketwo.backend.repository.MovieLogRepository;
import com.taketwo.backend.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MovieLogService {

    private final MovieLogRepository movieLogRepository;
    private final ReviewRepository reviewRepository;
    private final MovieCacheService movieCacheService;
    private final MovieMapper movieMapper;

    @Transactional
    public MovieLogResponse createLog(User user, LogMovieRequest request) {
        Movie movie = movieCacheService.findOrCacheMovie(request.tmdbId());

        MovieLog log = MovieLog.builder()
                .user(user)
                .movie(movie)
                .watchedDate(request.watchedDate())
                .rating(request.rating())
                .rewatch(request.rewatch())
                .build();
        log = movieLogRepository.save(log);

        Review review = null;
        if (request.reviewText() != null && !request.reviewText().isBlank()) {
            review = Review.builder()
                    .user(user)
                    .movie(movie)
                    .movieLog(log)
                    .reviewText(request.reviewText())
                    .containsSpoilers(Boolean.TRUE.equals(request.containsSpoilers()))
                    .build();
            review = reviewRepository.save(review);
        }

        return toResponse(log, review, movie);
    }

    public List<MovieLogResponse> getUserLogs(UUID userId) {
        return movieLogRepository.findByUser_IdOrderByWatchedDateDesc(userId).stream()
                .map(log -> toResponse(log, reviewRepository.findByMovieLog_Id(log.getId()).orElse(null), log.getMovie()))
                .toList();
    }

    private MovieLogResponse toResponse(MovieLog log, Review review, Movie movie) {
        return new MovieLogResponse(
                log.getId(),
                movieMapper.toSummary(movie),
                log.getWatchedDate(),
                log.getRating(),
                log.isRewatch(),
                review != null ? review.getReviewText() : null,
                review != null ? review.isContainsSpoilers() : null,
                log.getCreatedAt()
        );
    }
}
