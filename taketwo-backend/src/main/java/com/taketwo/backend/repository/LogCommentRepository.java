package com.taketwo.backend.repository;

import com.taketwo.backend.entity.LogComment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface LogCommentRepository extends JpaRepository<LogComment, UUID> {
    List<LogComment> findByMovieLog_IdOrderByCreatedAtAsc(UUID movieLogId);
    long countByMovieLog_Id(UUID movieLogId);
}
