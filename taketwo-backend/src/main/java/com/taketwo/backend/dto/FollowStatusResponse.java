package com.taketwo.backend.dto;

public record FollowStatusResponse(
        boolean following,
        long followerCount
) {}
