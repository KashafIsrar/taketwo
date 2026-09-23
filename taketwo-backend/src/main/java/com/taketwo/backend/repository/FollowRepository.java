package com.taketwo.backend.repository;

import com.taketwo.backend.entity.Follow;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FollowRepository extends JpaRepository<Follow, UUID> {

    Optional<Follow> findByFollower_IdAndFollowee_Id(UUID followerId, UUID followeeId);

    boolean existsByFollower_IdAndFollowee_Id(UUID followerId, UUID followeeId);

    // Who follows this user
    List<Follow> findByFollowee_IdOrderByCreatedAtDesc(UUID followeeId);

    // Who this user follows
    List<Follow> findByFollower_IdOrderByCreatedAtDesc(UUID followerId);

    long countByFollowee_Id(UUID followeeId);

    long countByFollower_Id(UUID followerId);
}
