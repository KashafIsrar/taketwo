package com.taketwo.backend.dto;

import java.util.UUID;

public record MemberSearchResponse(
        UUID id,
        String username,
        String displayName,
        boolean isFollowing
) {}