// repository/ChallengeProgressRepository.java
package com.taketwo.backend.repository;

import com.taketwo.backend.entity.ChallengeProgress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ChallengeProgressRepository extends JpaRepository<ChallengeProgress, UUID> {
    List<ChallengeProgress> findByUser_IdAndCompletedAtIsNull(UUID userId);
    List<ChallengeProgress> findByUser_Id(UUID userId);
}