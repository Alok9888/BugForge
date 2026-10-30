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
            "Results were produced in the browser sandbox. The server stored and cross-checked counts against the challenge definition, but did not re-execute the JavaScript.";

    private final ChallengeRepository challengeRepository;
    private final ChallengeProgressRepository progressRepository;
    private final SubmissionRepository submissionRepository;
    private final ChallengeService challengeService;
    private final ObjectMapper objectMapper;

    public SubmissionService(
            ChallengeRepository challengeRepository,
            ChallengeProgressRepository progressRepository,
            SubmissionRepository submissionRepository,
            ChallengeService challengeService,
            ObjectMapper objectMapper
    ) {
        this.challengeRepository = challengeRepository;
        this.progressRepository = progressRepository;
        this.submissionRepository = submissionRepository;
        this.challengeService = challengeService;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public SubmissionResponse submit(AppUser user, SubmissionRequest request) {
        Challenge challenge = challengeRepository.findBySlug(request.challengeSlug())
                .orElseThrow(() -> new NotFoundException("Challenge not found: " + request.challengeSlug()));

        int expected = challengeService.expectedTestCount(challenge);
        if (expected == 0) {
            throw new BadRequestException("Challenge has no tests configured");
        }
        if (request.results().size() != expected) {
            throw new BadRequestException("Result count does not match challenge tests");
        }

        Set<String> expectedIds = new HashSet<>();
        try {
            JsonNode cases = objectMapper.readTree(challenge.getTestsJson()).path("cases");
            cases.forEach(testCase -> {
                if (testCase.hasNonNull("id")) expectedIds.add(testCase.get("id").asText());
            });
        } catch (JsonProcessingException ex) {
            throw new BadRequestException("Challenge tests are invalid");
        }
        if (expectedIds.size() != expected) {
            throw new BadRequestException("Challenge test IDs must be present and unique");
        }
        Set<String> submittedIds = new HashSet<>();
        for (TestCaseResultDto result : request.results()) {
            if (!expectedIds.contains(result.id()) || !submittedIds.add(result.id())) {
                throw new BadRequestException("Test result IDs must match the challenge tests exactly");
            }
        }

        long passedFromResults = request.results().stream().filter(TestCaseResultDto::passed).count();
        long failedFromResults = request.results().size() - passedFromResults;
        if (request.passedCount() != passedFromResults || request.failedCount() != failedFromResults) {
            throw new BadRequestException("Reported pass/fail counts do not match result items");
        }

        boolean allPassed = failedFromResults == 0;
        SubmissionStatus status = allPassed ? SubmissionStatus.PASSED : SubmissionStatus.FAILED;
        Instant now = Instant.now();

        Submission submission = submissionRepository.save(new Submission(
                user,
                challenge,
                request.code(),
                status,
                request.passedCount(),
                request.failedCount(),
                expected,
                writeResults(request.results()),
                VerificationSource.CLIENT_BROWSER,
                now
        ));

        ChallengeProgress progress = progressRepository.findByUserAndChallenge(user, challenge)
                .orElseGet(() -> new ChallengeProgress(user, challenge));
        progress.recordAttempt(request.code(), request.passedCount(), request.failedCount(), allPassed, now);
        progressRepository.save(progress);

        return toResponse(submission, progress.isSolved(), request.results());
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
