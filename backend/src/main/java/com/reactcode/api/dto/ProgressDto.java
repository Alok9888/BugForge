package com.reactcode.api.dto;

import java.util.List;
import java.util.UUID;

public record ProgressDto(
        UUID userId,
        int solvedCount,
        int mvpChallengeCount,
        List<ChallengeProgressDto> challenges
) {
}
