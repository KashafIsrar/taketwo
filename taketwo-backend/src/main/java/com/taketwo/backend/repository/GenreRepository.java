package com.taketwo.backend.repository;

import com.taketwo.backend.entity.Genre;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface GenreRepository extends JpaRepository<Genre, UUID> {
    Optional<Genre> findByTmdbGenreId(Integer tmdbGenreId);
}
