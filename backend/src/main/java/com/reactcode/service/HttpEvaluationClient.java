package com.reactcode.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.reactcode.api.dto.TestCaseResultDto;
import com.reactcode.domain.Challenge;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;

@Component
public class HttpEvaluationClient implements EvaluationClient {
    private final RestClient client;
    private final ObjectMapper objectMapper;
    private final String evaluatorToken;

    public HttpEvaluationClient(RestClient.Builder builder,
                                ObjectMapper objectMapper,
                                @Value("${app.evaluator.url:http://localhost:8090}") String evaluatorUrl,
                                @Value("${app.evaluator.token:local-dev-only-change-me}") String evaluatorToken) {
        this.client = builder.baseUrl(evaluatorUrl).build();
        this.objectMapper = objectMapper;
        this.evaluatorToken = evaluatorToken;
    }

    @Override
    public EvaluationResult evaluate(Challenge challenge, String sourceCode) {
        try {
            RunnerResponse response = client.post()
                    .uri("/evaluate")
                    .header("X-Evaluator-Token", evaluatorToken)
                    .body(new RunnerRequest(challenge.getSlug(), sourceCode, parseTests(challenge)))
                    .retrieve()
                    .body(RunnerResponse.class);
            if (response == null || response.results() == null || response.totalCount() <= 0
                    || response.results().size() != response.totalCount()
                    || response.passedCount() + response.failedCount() != response.totalCount()) {
                throw new IllegalStateException("Evaluator returned an invalid result");
            }
            return new EvaluationResult(response.passedCount(), response.failedCount(), response.totalCount(), response.results());
        } catch (RestClientException ex) {
            throw new EvaluatorUnavailableException("The evaluation runner is unavailable. Please try again shortly.", ex);
        }
    }

    private JsonNode parseTests(Challenge challenge) {
        try {
            return objectMapper.readTree(challenge.getTestsJson());
        } catch (Exception ex) {
            throw new IllegalStateException("Challenge test configuration is invalid", ex);
        }
    }

    public record RunnerRequest(String problemSlug, String sourceCode, JsonNode tests) {}
    public record RunnerResponse(int passedCount, int failedCount, int totalCount, List<TestCaseResultDto> results) {}
}
