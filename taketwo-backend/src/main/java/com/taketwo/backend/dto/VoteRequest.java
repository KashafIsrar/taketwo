// dto/VoteRequest.java
package com.taketwo.backend.dto;

import jakarta.validation.constraints.NotNull;

public record VoteRequest(
        @NotNull VoteType voteType
) {
    public enum VoteType { UP, DOWN }
}