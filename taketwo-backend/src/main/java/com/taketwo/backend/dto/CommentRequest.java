package com.taketwo.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CommentRequest(
        @NotBlank
        @Size(max = 1000, message = "Comment must be under 1000 characters")
        String commentText
) {}
