package com.taketwo.backend.repository;

import com.taketwo.backend.entity.MovieLog;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface MovieLogRepository extends JpaRepository<MovieLog, UUID> {
    List<MovieLog> findByUser_IdOrderByWatchedDateDesc(UUID userId);

    long countByUser_Id(UUID userId);

    // Returns null (not 0.0) when the user has zero logs - handled explicitly
    // in ProfileService rather than silently showing "0.0" for someone who
    // hasn't logged anything yet.
    @Query("select avg(m.rating) from MovieLog m where m.user.id = :userId")
    Double findAverageRatingByUserId(@Param("userId") UUID userId);

    // Powers the activity feed: logs from any user in the given set (the
    // people the current user follows), most recent first, capped by the
    // page size passed in - avoids ever loading an unbounded feed.
    List<MovieLog> findByUser_IdInOrderByCreatedAtDesc(List<UUID> userIds, Pageable pageable);
}
