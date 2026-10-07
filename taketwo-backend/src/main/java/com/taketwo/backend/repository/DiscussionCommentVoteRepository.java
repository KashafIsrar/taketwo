// repository/DiscussionCommentVoteRepository.java
package com.taketwo.backend.repository;

import com.taketwo.backend.entity.DiscussionCommentVote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface DiscussionCommentVoteRepository extends JpaRepository<DiscussionCommentVote, UUID> {
    Optional<DiscussionCommentVote> findByComment_IdAndUser_Id(UUID commentId, UUID userId);

    @Query("""
            select coalesce(sum(case when v.voteType = com.taketwo.backend.entity.DiscussionCommentVote.VoteType.UP then 1 else -1 end), 0)
            from DiscussionCommentVote v where v.comment.id = :commentId
            """)
    long computeScore(@Param("commentId") UUID commentId);
}