package com.taketwo.backend.service;

import com.taketwo.backend.dto.ActivityFeedItemResponse;
import com.taketwo.backend.dto.FollowUserSummary;
import com.taketwo.backend.entity.MovieLog;
import com.taketwo.backend.entity.User;
import com.taketwo.backend.repository.FollowRepository;
import com.taketwo.backend.repository.MovieLogRepository;
import com.taketwo.backend.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ActivityFeedService {

    private static final int DEFAULT_LIMIT = 30;
    private static final int MAX_LIMIT = 100;

    private final FollowRepository followRepository;
    private final MovieLogRepository movieLogRepository;
    private final ReviewRepository reviewRepository;
    private final MovieMapper movieMapper;

    public List<ActivityFeedItemResponse> getFeed(User currentUser, Integer requestedLimit) {
        List<UUID> followingIds = followRepository.findByFollower_IdOrderByCreatedAtDesc(currentUser.getId())
                .stream()
                .map(f -> f.getFollowee().getId())
                .toList();

        // Nobody followed yet - short-circuit rather than issuing a query
        // with an empty IN clause, which is wasted work either way.
        if (followingIds.isEmpty()) {
            return Collections.emptyList();
        }

        int limit = clampLimit(requestedLimit);
        List<MovieLog> logs = movieLogRepository.findByUser_IdInOrderByCreatedAtDesc(followingIds, PageRequest.of(0, limit));

        return logs.stream().map(this::toFeedItem).toList();
    }

    private int clampLimit(Integer requested) {
        if (requested == null || requested < 1) return DEFAULT_LIMIT;
        return Math.min(requested, MAX_LIMIT);
    }

    private ActivityFeedItemResponse toFeedItem(MovieLog log) {
        var review = reviewRepository.findByMovieLog_Id(log.getId()).orElse(null);
        User loggedBy = log.getUser();

        return new ActivityFeedItemResponse(
                log.getId(),
                new FollowUserSummary(loggedBy.getId(), loggedBy.getUsername(), loggedBy.getDisplayName()),
                movieMapper.toSummary(log.getMovie()),
                log.getWatchedDate(),
                log.getRating(),
                log.isRewatch(),
                review != null ? review.getReviewText() : null,
                review != null ? review.isContainsSpoilers() : null,
                log.getCreatedAt()
        );
    }
}
