package com.reactcode.api.dto;

import com.reactcode.domain.Difficulty;
import com.fasterxml.jackson.databind.JsonNode;

public record ChallengeDetailDto(
        Long id,
        String slug,
        Integer sortOrder,
        String title,
        Difficulty difficulty,
        String shortDescription,
        String statement,
        String starterCode,
        JsonNode tests,
        boolean mvpAvailable,
        boolean locked,
        boolean solved
) {
}
