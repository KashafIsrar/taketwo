// dto/SendMessageRequest.java
package com.taketwo.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SendMessageRequest(
        @NotBlank
        @Size(max = 2000, message = "Message must be under 2000 characters")
        String content
) {}