package com.reactcode.api.dto;

import java.time.Instant;

public record ChallengeProgressDto(
        String challengeSlug,
        boolean solved,
        Instant solvedAt,
        int attemptCount,
        Integer lastPassedCount,
        Integer lastFailedCount,
        Instant updatedAt
) {
}
