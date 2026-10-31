package com.reactcode.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SubmissionRequest(
        @NotBlank @Size(max = 64) String challengeSlug,
        @NotBlank @Size(max = 100_000) String code
) {
}
