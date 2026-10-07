// dto/ParticipantResponse.java
package com.taketwo.backend.dto;

import java.util.UUID;

public record ParticipantResponse(
        UUID userId,
        String username,
        String displayName,
        String role   // "ADMIN" | "MEMBER"
) {}