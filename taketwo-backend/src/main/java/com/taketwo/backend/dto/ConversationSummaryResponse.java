// dto/ConversationSummaryResponse.java
package com.taketwo.backend.dto;

import java.time.Instant;
import java.util.UUID;

public record ConversationSummaryResponse(
        UUID conversationId,
        FollowUserSummary otherUser,   // null for GROUP conversations once those exist - step 1 is DM-only
        String lastMessagePreview,     // null if no messages sent yet
        Instant lastMessageAt,
        boolean unread
) {}