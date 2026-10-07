// dto/DiscussionPostResponse.java
package com.taketwo.backend.dto;

import java.time.Instant;
import java.util.UUID;

public record DiscussionPostResponse(
        UUID postId,
        MovieSummaryResponse movie,
        FollowUserSummary user,
        String title,
        String body,
        boolean isSpoiler,
        long score,
        String myVote,
        long commentCount,
        Instant createdAt
) {}