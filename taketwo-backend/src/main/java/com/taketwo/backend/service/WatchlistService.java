package com.taketwo.backend.service;

import com.taketwo.backend.dto.WatchlistItemResponse;
import com.taketwo.backend.dto.WatchlistToggleResponse;
import com.taketwo.backend.entity.Movie;
import com.taketwo.backend.entity.User;
import com.taketwo.backend.entity.WatchlistItem;
import com.taketwo.backend.repository.MovieRepository;
import com.taketwo.backend.repository.WatchlistItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WatchlistService {

    private final WatchlistItemRepository watchlistItemRepository;
    private final MovieRepository movieRepository;
    private final MovieCacheService movieCacheService;
    private final MovieMapper movieMapper;

    // Deliberately does NOT go through MovieCacheService: checking status
    // isn't a "view" or a "log", so it shouldn't trigger caching. If the
    // movie was never cached, it can't possibly be on anyone's watchlist.
    public boolean isOnWatchlist(UUID userId, Long tmdbId) {
        return movieRepository.findByTmdbId(tmdbId)
                .flatMap(movie -> watchlistItemRepository.findByUser_IdAndMovie_Id(userId, movie.getId()))
                .isPresent();
    }

    @Transactional
    public WatchlistToggleResponse toggle(User user, Long tmdbId) {
        Movie movie = movieCacheService.findOrCacheMovie(tmdbId);

        return watchlistItemRepository.findByUser_IdAndMovie_Id(user.getId(), movie.getId())
                .map(existing -> {
                    watchlistItemRepository.delete(existing);
                    return new WatchlistToggleResponse(false, movieMapper.toSummary(movie));
                })
                .orElseGet(() -> {
                    WatchlistItem item = WatchlistItem.builder().user(user).movie(movie).build();
                    watchlistItemRepository.save(item);
                    return new WatchlistToggleResponse(true, movieMapper.toSummary(movie));
                });
    }

    public List<WatchlistItemResponse> getUserWatchlist(UUID userId) {
        return watchlistItemRepository.findByUser_IdOrderByAddedAtDesc(userId).stream()
                .map(item -> new WatchlistItemResponse(movieMapper.toSummary(item.getMovie()), item.getAddedAt()))
                .toList();
    }
}
