package com.reactcode.seed;

import com.reactcode.domain.Difficulty;

public record SeedChallenge(
        String slug,
        String title,
        Difficulty difficulty,
        String shortDescription,
        String statementMd,
        String starterCode,
        String testsJson,
        int sortOrder,
        boolean availableInMvp
) {
}
