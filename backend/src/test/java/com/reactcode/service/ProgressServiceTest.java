package com.reactcode.service;

import com.reactcode.ReactCodeApplication;
import com.reactcode.api.dto.SubmissionRequest;
import com.reactcode.api.dto.TestCaseResultDto;
import com.reactcode.domain.AppUser;
import com.reactcode.domain.Challenge;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = ReactCodeApplication.class)
class ProgressServiceTest {

    @Autowired private UserService users;
    @Autowired private SubmissionService submissions;
    @Autowired private ProgressService progress;
    @MockBean private EvaluationClient evaluator;

    @org.junit.jupiter.api.BeforeEach
    void configureEvaluator() {
        when(evaluator.evaluate(any(Challenge.class), anyString())).thenAnswer(invocation -> {
            boolean pass = "pass".equals(invocation.getArgument(1));
            var results = List.of(result("starts-at-zero", true), result("increment-once", pass), result("increment-twice", true));
            return new EvaluationResult(pass ? 3 : 2, pass ? 0 : 1, 3, results);
        });
    }

    @Test
    void failedSubmissionRecordsAttemptWithoutMarkingSolved() {
        AppUser user = users.resolveOrCreate(UUID.randomUUID().toString());
        submissions.submit(user, request("fail"));

        var item = progress.getChallengeProgress(user, "counter");
        assertThat(item.solved()).isFalse();
        assertThat(item.attemptCount()).isEqualTo(1);
        assertThat(item.lastFailedCount()).isEqualTo(1);
    }

    @Test
    void successfulSubmissionMarksChallengeSolved() {
        AppUser user = users.resolveOrCreate(UUID.randomUUID().toString());
        submissions.submit(user, request("pass"));

        var item = progress.getChallengeProgress(user, "counter");
        assertThat(item.solved()).isTrue();
        assertThat(item.attemptCount()).isEqualTo(1);
    }

    private SubmissionRequest request(String code) {
        return new SubmissionRequest("counter", code);
    }

    private TestCaseResultDto result(String id, boolean passed) {
        return new TestCaseResultDto(id, id, passed, null);
    }
}
