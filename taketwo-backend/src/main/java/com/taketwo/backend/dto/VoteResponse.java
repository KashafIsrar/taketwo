// dto/VoteResponse.java
package com.taketwo.backend.dto;

public record VoteResponse(
        long score,
        String myVote
) {}