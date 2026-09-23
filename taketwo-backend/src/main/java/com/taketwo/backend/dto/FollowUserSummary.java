package com.taketwo.backend.dto;

import java.util.UUID;

public record FollowUserSummary(
        UUID userId,
        String username,
        String displayName
) {}
