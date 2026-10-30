package com.reactcode.service;

import com.reactcode.ReactCodeApplication;
import com.reactcode.api.dto.SubmissionRequest;
import com.reactcode.api.dto.TestCaseResultDto;
import com.reactcode.domain.AppUser;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = ReactCodeApplication.class)
class ProgressServiceTest {

    @Autowired private UserService users;
    @Autowired private SubmissionService submissions;
    @Autowired private ProgressService progress;

    @Test
    void failedSubmissionRecordsAttemptWithoutMarkingSolved() {
        AppUser user = users.resolveOrCreate(UUID.randomUUID().toString());
        submissions.submit(user, request(false));

        var item = progress.getChallengeProgress(user, "counter");
        assertThat(item.solved()).isFalse();
        assertThat(item.attemptCount()).isEqualTo(1);
        assertThat(item.lastFailedCount()).isEqualTo(1);
    }

    @Test
    void successfulSubmissionMarksChallengeSolved() {
        AppUser user = users.resolveOrCreate(UUID.randomUUID().toString());
        submissions.submit(user, request(true));

        var item = progress.getChallengeProgress(user, "counter");
        assertThat(item.solved()).isTrue();
        assertThat(item.attemptCount()).isEqualTo(1);
    }

    private SubmissionRequest request(boolean allPassed) {
        return new SubmissionRequest("counter", "code", allPassed ? 3 : 2, allPassed ? 0 : 1, List.of(
                result("starts-at-zero", true),
                result("increment-once", allPassed),
                result("increment-twice", true)
        ));
    }

    private TestCaseResultDto result(String id, boolean passed) {
        return new TestCaseResultDto(id, id, passed, null);
    }
}
