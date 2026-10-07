// repository/DiscussionCommentRepository.java
package com.taketwo.backend.repository;

import com.taketwo.backend.entity.DiscussionComment;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface DiscussionCommentRepository extends JpaRepository<DiscussionComment, UUID> {
    List<DiscussionComment> findByPost_IdAndParentComment_IdIsNullOrderByCreatedAtAsc(UUID postId);
    List<DiscussionComment> findByParentComment_IdOrderByCreatedAtAsc(UUID parentCommentId, Pageable pageable);
    long countByParentComment_Id(UUID parentCommentId);
    long countByPost_Id(UUID postId);
}