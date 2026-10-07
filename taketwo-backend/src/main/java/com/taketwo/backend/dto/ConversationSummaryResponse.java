package com.taketwo.backend.dto;

import java.time.Instant;
import java.util.UUID;

public record ConversationSummaryResponse(
        UUID conversationId,
        String type,                // "DM" | "GROUP"
        FollowUserSummary otherUser, // null for GROUP
        String groupName,           // null for DM
        int participantCount,
        String lastMessagePreview,
        Instant lastMessageAt,
        boolean unread
) {}