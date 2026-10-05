// dto/projection/KeywordAffinityProjection.java
package com.taketwo.backend.dto.projection;

public record KeywordAffinityProjection(String keywordName, long filmCount, Double averageRating) {}