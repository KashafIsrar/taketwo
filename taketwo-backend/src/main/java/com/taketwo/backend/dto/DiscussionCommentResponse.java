// dto/DiscussionCommentResponse.java
package com.taketwo.backend.dto;

import java.time.Instant;
import java.util.UUID;

public record DiscussionCommentResponse(
        UUID commentId,
        UUID postId,
        UUID parentCommentId,
        FollowUserSummary user,
        String body,
        boolean isSpoiler,
        long score,
        String myVote,
        long replyCount,
        Instant createdAt
) {}