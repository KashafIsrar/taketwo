// dto/DirectorAffinity.java
package com.taketwo.backend.dto;

import java.util.UUID;

public record DirectorAffinity(
        UUID personId,
        Long tmdbPersonId,
        String name,
        long filmCount,
        Double averageRating
) {}