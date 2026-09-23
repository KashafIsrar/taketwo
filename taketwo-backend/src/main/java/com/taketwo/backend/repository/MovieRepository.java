package com.taketwo.backend.repository;

import com.taketwo.backend.entity.Movie;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface MovieRepository extends JpaRepository<Movie, UUID> {
    Optional<Movie> findByTmdbId(Long tmdbId);
}
