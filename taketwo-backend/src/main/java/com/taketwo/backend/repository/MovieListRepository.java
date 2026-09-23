package com.taketwo.backend.repository;

import com.taketwo.backend.entity.MovieList;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface MovieListRepository extends JpaRepository<MovieList, UUID> {
    List<MovieList> findByUserIdOrderByCreatedAtDesc(UUID userId);
    List<MovieList> findByUserIdAndIsPrivateFalseOrderByCreatedAtDesc(UUID userId);
}