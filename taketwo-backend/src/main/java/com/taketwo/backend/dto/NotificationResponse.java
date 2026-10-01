// dto/NotificationResponse.java
package com.taketwo.backend.dto;

import com.taketwo.backend.entity.Notification;

import java.time.Instant;
import java.util.UUID;

public record NotificationResponse(
        UUID id,
        Notification.Type type,
        FollowUserSummary actor,
        MovieSummaryResponse movie,
        String commentPreview,   // null for LIKE notifications
        boolean read,
        Instant createdAt
) {}