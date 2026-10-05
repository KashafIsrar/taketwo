// repository/KeywordRepository.java
package com.taketwo.backend.repository;

import com.taketwo.backend.entity.Keyword;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface KeywordRepository extends JpaRepository<Keyword, UUID> {
    Optional<Keyword> findByTmdbKeywordId(Integer tmdbKeywordId);
}