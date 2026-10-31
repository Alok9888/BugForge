package com.reactcode.service;

import com.reactcode.domain.Challenge;

public interface EvaluationClient {
    EvaluationResult evaluate(Challenge challenge, String sourceCode);
}
