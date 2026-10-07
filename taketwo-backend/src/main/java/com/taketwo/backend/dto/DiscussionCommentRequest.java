// dto/DiscussionCommentRequest.java
package com.taketwo.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record DiscussionCommentRequest(
        @NotBlank @Size(max = 3000) String body,
        boolean isSpoiler,
        UUID parentCommentId
) {}