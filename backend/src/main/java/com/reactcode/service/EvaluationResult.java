package com.reactcode.service;

import com.reactcode.api.dto.TestCaseResultDto;

import java.util.List;

public record EvaluationResult(int passedCount, int failedCount, int totalCount, List<TestCaseResultDto> results) {
    public boolean passed() {
        return totalCount > 0 && passedCount == totalCount && failedCount == 0;
    }
}
