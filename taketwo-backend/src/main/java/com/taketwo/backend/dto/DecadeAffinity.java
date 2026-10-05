// dto/DecadeAffinity.java
package com.taketwo.backend.dto;

public record DecadeAffinity(
        int decade,         // e.g. 1990 means "1990s"
        long filmCount,
        Double averageRating
) {}