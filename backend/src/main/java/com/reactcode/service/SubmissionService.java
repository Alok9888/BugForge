package com.reactcode.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.reactcode.api.BadRequestException;
import com.reactcode.api.NotFoundException;
import com.reactcode.api.dto.SubmissionRequest;
import com.reactcode.api.dto.SubmissionResponse;
import com.reactcode.api.dto.TestCaseResultDto;
import com.reactcode.domain.AppUser;
import com.reactcode.domain.Challenge;
import com.reactcode.domain.ChallengeProgress;
import com.reactcode.domain.Submission;
import com.reactcode.domain.SubmissionStatus;
import com.reactcode.domain.VerificationSource;
import com.reactcode.repo.ChallengeProgressRepository;
import com.reactcode.repo.ChallengeRepository;
import com.reactcode.repo.SubmissionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class SubmissionService {

    static final String VERIFICATION_NOTE =
            "The server evaluated your component in an isolated browser runner against the challenge's managed tests.";

    private final ChallengeRepository challengeRepository;
    private final ChallengeProgressRepository progressRepository;
    private final SubmissionRepository submissionRepository;
    private final ChallengeService challengeService;
    private final ObjectMapper objectMapper;
    private final EvaluationClient evaluator;

    public SubmissionService(
            ChallengeRepository challengeRepository,
            ChallengeProgressRepository progressRepository,
            SubmissionRepository submissionRepository,
            ChallengeService challengeService,
            ObjectMapper objectMapper,
            EvaluationClient evaluator
    ) {
        this.challengeRepository = challengeRepository;
        this.progressRepository = progressRepository;
        this.submissionRepository = submissionRepository;
        this.challengeService = challengeService;
        this.objectMapper = objectMapper;
        this.evaluator = evaluator;
    }

    @Transactional
    public SubmissionResponse submit(AppUser user, SubmissionRequest request) {
        Challenge challenge = challengeRepository.findBySlug(request.challengeSlug())
                .orElseThrow(() -> new NotFoundException("Challenge not found: " + request.challengeSlug()));

        EvaluationResult evaluation = evaluator.evaluate(challenge, request.code());
        int expected = challengeService.expectedTestCount(challenge);
        if (expected == 0 || evaluation.totalCount() != expected || evaluation.results().size() != expected) {
            throw new IllegalStateException("Evaluation runner returned the wrong number of test results");
        }
        Set<String> expectedIds = new HashSet<>();
        Set<String> resultIds = new HashSet<>();
        try {
            JsonNode cases = objectMapper.readTree(challenge.getTestsJson()).path("cases");
            cases.forEach(item -> { if (item.hasNonNull("id")) expectedIds.add(item.get("id").asText()); });
            evaluation.results().forEach(item -> { if (item != null && item.id() != null) resultIds.add(item.id()); });
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Challenge test configuration is invalid", ex);
        }
        if (expectedIds.size() != expected || resultIds.size() != expected || !expectedIds.equals(resultIds)) {
            throw new IllegalStateException("Evaluation runner returned mismatched test cases");
        }
        long passedFromResults = evaluation.results().stream().filter(TestCaseResultDto::passed).count();
        if (passedFromResults != evaluation.passedCount()
                || evaluation.failedCount() != evaluation.totalCount() - evaluation.passedCount()) {
            throw new IllegalStateException("Evaluation runner returned inconsistent test results");
        }
        boolean allPassed = evaluation.passed();
        SubmissionStatus status = allPassed ? SubmissionStatus.PASSED : SubmissionStatus.FAILED;
        Instant now = Instant.now();

        Submission submission = submissionRepository.save(new Submission(
                user,
                challenge,
                request.code(),
                status,
                evaluation.passedCount(),
                evaluation.failedCount(),
                expected,
                writeResults(evaluation.results()),
                VerificationSource.SERVER_RUNNER,
                now
        ));

        ChallengeProgress progress = progressRepository.findByUserAndChallenge(user, challenge)
                .orElseGet(() -> new ChallengeProgress(user, challenge));
        progress.recordAttempt(request.code(), evaluation.passedCount(), evaluation.failedCount(), allPassed, now);
        progressRepository.save(progress);

        return toResponse(submission, progress.isSolved(), evaluation.results());
    }

    @Transactional(readOnly = true)
    public SubmissionResponse get(AppUser user, Long id) {
        Submission submission = submissionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Submission not found: " + id));
        if (!submission.getUser().getId().equals(user.getId())) {
            throw new NotFoundException("Submission not found: " + id);
        }
        ChallengeProgress progress = progressRepository
                .findByUserAndChallenge(submission.getUser(), submission.getChallenge())
                .orElse(null);
        boolean solved = progress != null && progress.isSolved();
        return toResponse(submission, solved, readResults(submission.getResultsJson()));
    }

    private SubmissionResponse toResponse(Submission submission, boolean solved, List<TestCaseResultDto> results) {
        return new SubmissionResponse(
                submission.getId(),
                submission.getChallenge().getSlug(),
                submission.getStatus(),
                submission.getPassedCount(),
                submission.getFailedCount(),
                submission.getTotalCount(),
                solved,
                submission.getVerificationSource(),
                VERIFICATION_NOTE,
                results,
                submission.getCreatedAt()
        );
    }

    private String writeResults(List<TestCaseResultDto> results) {
        try {
            return objectMapper.writeValueAsString(results);
        } catch (JsonProcessingException ex) {
            throw new BadRequestException("Unable to serialize test results");
        }
    }

    private List<TestCaseResultDto> readResults(String json) {
        try {
            return objectMapper.readerForListOf(TestCaseResultDto.class).readValue(json);
        } catch (Exception ex) {
            return List.of();
        }
    }
}
