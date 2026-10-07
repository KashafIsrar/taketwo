// repository/DiscussionPostRepository.java
package com.taketwo.backend.repository;

import com.taketwo.backend.entity.DiscussionPost;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface DiscussionPostRepository extends JpaRepository<DiscussionPost, UUID> {
    List<DiscussionPost> findByMovie_IdOrderByCreatedAtDesc(UUID movieId, Pageable pageable);
}