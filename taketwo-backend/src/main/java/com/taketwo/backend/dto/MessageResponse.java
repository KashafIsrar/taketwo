// dto/MessageResponse.java
package com.taketwo.backend.dto;

import java.time.Instant;
import java.util.UUID;

public record MessageResponse(
        UUID messageId,
        UUID conversationId,
        FollowUserSummary sender,
        String content,
        Instant createdAt
) {}