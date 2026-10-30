package com.reactcode.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record TestCaseResultDto(
        @NotBlank String id,
        @NotBlank String name,
        @NotNull Boolean passed,
        String message
) {
}
