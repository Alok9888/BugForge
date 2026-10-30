package com.reactcode.api.dto;

import com.reactcode.domain.SubmissionStatus;
import com.reactcode.domain.VerificationSource;

import java.time.Instant;
import java.util.List;

public record SubmissionResponse(
        Long id,
        String challengeSlug,
        SubmissionStatus status,
        int passedCount,
        int failedCount,
        int totalCount,
        boolean solved,
        VerificationSource verificationSource,
        String verificationNote,
        List<TestCaseResultDto> results,
        Instant createdAt
) {
}
