package com.taketwo.backend.repository;

import com.taketwo.backend.entity.LogLike;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface LogLikeRepository extends JpaRepository<LogLike, UUID> {
    Optional<LogLike> findByUser_IdAndMovieLog_Id(UUID userId, UUID movieLogId);
    boolean existsByUser_IdAndMovieLog_Id(UUID userId, UUID movieLogId);
    long countByMovieLog_Id(UUID movieLogId);
}
