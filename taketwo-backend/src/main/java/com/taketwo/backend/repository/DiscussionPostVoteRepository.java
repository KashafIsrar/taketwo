// repository/DiscussionPostVoteRepository.java
package com.taketwo.backend.repository;

import com.taketwo.backend.entity.DiscussionPostVote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface DiscussionPostVoteRepository extends JpaRepository<DiscussionPostVote, UUID> {
    Optional<DiscussionPostVote> findByPost_IdAndUser_Id(UUID postId, UUID userId);

    @Query("""
            select coalesce(sum(case when v.voteType = com.taketwo.backend.entity.DiscussionPostVote.VoteType.UP then 1 else -1 end), 0)
            from DiscussionPostVote v where v.post.id = :postId
            """)
    long computeScore(@Param("postId") UUID postId);
}