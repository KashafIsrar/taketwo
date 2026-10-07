// entity/DiscussionCommentVote.java
package com.taketwo.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "discussion_comment_votes", uniqueConstraints = @UniqueConstraint(columnNames = {"comment_id", "user_id"}))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiscussionCommentVote {

    public enum VoteType { UP, DOWN }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "comment_id", nullable = false)
    private DiscussionComment comment;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "vote_type", nullable = false, length = 10)
    private VoteType voteType;
}