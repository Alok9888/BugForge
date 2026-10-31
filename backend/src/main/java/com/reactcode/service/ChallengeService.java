package com.reactcode.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.reactcode.api.NotFoundException;
import com.reactcode.api.dto.ChallengeDetailDto;
import com.reactcode.api.dto.ChallengeSummaryDto;
import com.reactcode.domain.AppUser;
import com.reactcode.domain.Challenge;
import com.reactcode.domain.ChallengeProgress;
import com.reactcode.domain.Difficulty;
import com.reactcode.repo.ChallengeProgressRepository;
import com.reactcode.repo.ChallengeRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ChallengeService {

    private final ChallengeRepository challengeRepository;
    private final ChallengeProgressRepository progressRepository;
    private final ObjectMapper objectMapper;

    public ChallengeService(
            ChallengeRepository challengeRepository,
            ChallengeProgressRepository progressRepository,
            ObjectMapper objectMapper
    ) {
        this.challengeRepository = challengeRepository;
        this.progressRepository = progressRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public Page<ChallengeSummaryDto> list(
            AppUser user,
            String query,
            Difficulty difficulty,
            String catalog,
            int page,
            int size
    ) {
        Specification<Challenge> spec = Specification.where(null);

        if (!"all".equalsIgnoreCase(catalog)) {
            spec = spec.and((root, q, cb) -> cb.isTrue(root.get("mvpAvailable")));
        }
        if (difficulty != null) {
            spec = spec.and((root, q, cb) -> cb.equal(root.get("difficulty"), difficulty));
        }
        if (query != null && !query.isBlank()) {
            String like = "%" + query.trim().toLowerCase(Locale.ROOT) + "%";
            spec = spec.and((root, q, cb) -> cb.or(
                    cb.like(cb.lower(root.get("title")), like),
                    cb.like(cb.lower(root.get("shortDescription")), like),
                    cb.like(cb.lower(root.get("slug")), like)
            ));
        }

        Page<Challenge> challenges = challengeRepository.findAll(
                spec,
                PageRequest.of(page, size, Sort.by("sortOrder").ascending())
        );

        Set<Long> solvedIds = progressRepository.findByUserId(user.getId()).stream()
                .filter(ChallengeProgress::isSolved)
                .map(progress -> progress.getChallenge().getId())
                .collect(Collectors.toSet());

        return challenges.map(challenge -> toSummary(challenge, solvedIds.contains(challenge.getId())));
    }

    @Transactional(readOnly = true)
    public ChallengeDetailDto getBySlug(AppUser user, String slug) {
        Challenge challenge = challengeRepository.findBySlug(slug)
                .orElseThrow(() -> new NotFoundException("Challenge not found: " + slug));
        boolean solved = progressRepository.findByUserAndChallenge(user, challenge)
                .map(ChallengeProgress::isSolved)
                .orElse(false);
        return toDetail(challenge, solved);
    }

    @Transactional(readOnly = true)
    public Map<String, Boolean> solvedBySlug(AppUser user) {
        return progressRepository.findByUserId(user.getId()).stream()
                .collect(Collectors.toMap(
                        progress -> progress.getChallenge().getSlug(),
                        ChallengeProgress::isSolved
                ));
    }

    public int expectedTestCount(Challenge challenge) {
        try {
            JsonNode root = objectMapper.readTree(challenge.getTestsJson());
            JsonNode cases = root.get("cases");
            if (cases == null || !cases.isArray()) {
                return 0;
            }
            return cases.size();
        } catch (JsonProcessingException ex) {
            return 0;
        }
    }

    private ChallengeSummaryDto toSummary(Challenge challenge, boolean solved) {
        return new ChallengeSummaryDto(
                challenge.getId(),
                challenge.getSlug(),
                challenge.getSortOrder(),
                challenge.getTitle(),
                challenge.getDifficulty(),
                challenge.getShortDescription(),
                challenge.isMvpAvailable(),
                !challenge.isMvpAvailable(),
                solved
        );
    }

    private ChallengeDetailDto toDetail(Challenge challenge, boolean solved) {
        return new ChallengeDetailDto(
                challenge.getId(),
                challenge.getSlug(),
                challenge.getSortOrder(),
                challenge.getTitle(),
                challenge.getDifficulty(),
                challenge.getShortDescription(),
                challenge.getStatement(),
                challenge.getStarterCode(),
                challenge.isMvpAvailable(),
                !challenge.isMvpAvailable(),
                solved
        );
    }
}
