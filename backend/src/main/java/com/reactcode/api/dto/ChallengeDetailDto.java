package com.reactcode.api.dto;

import com.reactcode.domain.Difficulty;
public record ChallengeDetailDto(
        Long id,
        String slug,
        Integer sortOrder,
        String title,
        Difficulty difficulty,
        String shortDescription,
        String statement,
        String starterCode,
        boolean mvpAvailable,
        boolean locked,
        boolean solved
) {
}
