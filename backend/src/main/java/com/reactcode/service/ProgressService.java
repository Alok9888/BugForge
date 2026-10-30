package com.reactcode.service;

import com.reactcode.api.NotFoundException;
import com.reactcode.api.dto.ChallengeProgressDto;
import com.reactcode.api.dto.ProgressDto;
import com.reactcode.domain.AppUser;
import com.reactcode.domain.Challenge;
import com.reactcode.domain.ChallengeProgress;
import com.reactcode.repo.ChallengeProgressRepository;
import com.reactcode.repo.ChallengeRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProgressService {

    private final ChallengeProgressRepository progressRepository;
    private final ChallengeRepository challengeRepository;
    private final int mvpSize;

    public ProgressService(
            ChallengeProgressRepository progressRepository,
            ChallengeRepository challengeRepository,
            @Value("${app.catalog.mvp-size}") int mvpSize
    ) {
        this.progressRepository = progressRepository;
        this.challengeRepository = challengeRepository;
        this.mvpSize = mvpSize;
    }

    @Transactional(readOnly = true)
    public ProgressDto getProgress(AppUser user) {
        List<ChallengeProgressDto> items = progressRepository.findByUserId(user.getId()).stream()
                .map(this::toDto)
                .toList();
        int solved = (int) progressRepository.countByUserIdAndSolvedTrue(user.getId());
        return new ProgressDto(user.getId(), solved, mvpSize, items);
    }

    @Transactional(readOnly = true)
    public ChallengeProgressDto getChallengeProgress(AppUser user, String slug) {
        Challenge challenge = challengeRepository.findBySlug(slug)
                .orElseThrow(() -> new NotFoundException("Challenge not found: " + slug));
        return progressRepository.findByUserAndChallenge(user, challenge)
                .map(this::toDto)
                .orElse(new ChallengeProgressDto(slug, false, null, 0, null, null, null));
    }

    private ChallengeProgressDto toDto(ChallengeProgress progress) {
        return new ChallengeProgressDto(
                progress.getChallenge().getSlug(),
                progress.isSolved(),
                progress.getSolvedAt(),
                progress.getAttemptCount(),
                progress.getLastPassedCount(),
                progress.getLastFailedCount(),
                progress.getUpdatedAt()
        );
    }
}
