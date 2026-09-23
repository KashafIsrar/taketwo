package com.taketwo.backend.dto;

public record LikeStatusResponse(
        boolean liked,
        long likeCount
) {}
