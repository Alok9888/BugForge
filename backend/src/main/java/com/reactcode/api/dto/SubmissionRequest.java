package com.reactcode.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record SubmissionRequest(
        @NotBlank String challengeSlug,
        @NotBlank String code,
        @NotNull @Min(0) Integer passedCount,
        @NotNull @Min(0) Integer failedCount,
        @NotNull @Valid @Size(min = 1) List<TestCaseResultDto> results
) {
}
