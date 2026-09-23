package com.taketwo.backend.repository;

import com.taketwo.backend.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ReviewRepository extends JpaRepository<Review, UUID> {
    Optional<Review> findByMovieLog_Id(UUID movieLogId);
}
