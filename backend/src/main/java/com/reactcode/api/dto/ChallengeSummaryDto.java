package com.reactcode.api.dto;

import com.reactcode.domain.Difficulty;

public record ChallengeSummaryDto(
        Long id,
        String slug,
        Integer sortOrder,
        String title,
        Difficulty difficulty,
        String shortDescription,
        boolean mvpAvailable,
        boolean locked,
        boolean solved
) {
}
