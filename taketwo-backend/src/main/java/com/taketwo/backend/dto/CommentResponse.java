package com.taketwo.backend.dto;

import java.time.Instant;
import java.util.UUID;

public record CommentResponse(
        UUID commentId,
        FollowUserSummary user,
        String commentText,
        Instant createdAt
) {}
