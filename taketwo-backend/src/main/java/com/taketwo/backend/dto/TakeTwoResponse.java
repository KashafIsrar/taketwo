// dto/TakeTwoResponse.java
package com.taketwo.backend.dto;

public record TakeTwoResponse(
        MovieSummaryResponse movie,
        int matchPercent,          // a simple, transparent weighted score - not a real confidence interval
        String explanation,        // generated from the actual signals that produced this pick
        java.util.List<String> reasonTags
) {}