package com.taketwo.backend.repository;

import com.taketwo.backend.entity.MovieListItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface MovieListItemRepository extends JpaRepository<MovieListItem, UUID> {
    void deleteByMovieListIdAndMovieTmdbId(UUID listId, Long tmdbId);
}