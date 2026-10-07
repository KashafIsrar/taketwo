package com.taketwo.backend.repository;

import com.taketwo.backend.dto.FollowUserSummary;
import com.taketwo.backend.entity.Follow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FollowRepository extends JpaRepository<Follow, UUID> {

    Optional<Follow> findByFollower_IdAndFollowee_Id(UUID followerId, UUID followeeId);

    boolean existsByFollower_IdAndFollowee_Id(UUID followerId, UUID followeeId);

    // Who follows this user
    List<Follow> findByFollowee_IdOrderByCreatedAtDesc(UUID followeeId);

    // Who this user follows
    List<Follow> findByFollower_IdOrderByCreatedAtDesc(UUID followerId);

    long countByFollowee_Id(UUID followeeId);

    long countByFollower_Id(UUID followerId);

    @Query("""
        select new com.taketwo.backend.dto.FollowUserSummary(u.id, u.username, u.displayName)
        from Follow f1 join f1.followee u
        where f1.follower.id = :userId
        and exists (select 1 from Follow f2 where f2.follower.id = f1.followee.id and f2.followee.id = :userId)
        """)
    List<FollowUserSummary> findMutualFollowers(@Param("userId") UUID userId);
}