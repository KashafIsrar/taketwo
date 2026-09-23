package com.taketwo.backend.repository;

import com.taketwo.backend.entity.WatchlistItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WatchlistItemRepository extends JpaRepository<WatchlistItem, UUID> {
    List<WatchlistItem> findByUser_IdOrderByAddedAtDesc(UUID userId);
    Optional<WatchlistItem> findByUser_IdAndMovie_Id(UUID userId, UUID movieId);
}
