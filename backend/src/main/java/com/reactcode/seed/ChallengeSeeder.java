package com.reactcode.seed;

import com.reactcode.domain.Challenge;
import com.reactcode.repo.ChallengeRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Component
public class ChallengeSeeder implements ApplicationRunner {

    private final ChallengeRepository challengeRepository;
    public ChallengeSeeder(ChallengeRepository challengeRepository) {
        this.challengeRepository = challengeRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) throws Exception {
        List<SeedChallenge> items = ChallengeCatalog.all();
        Instant now = Instant.now();
        for (SeedChallenge item : items) {
            Challenge challenge = challengeRepository.findBySlug(item.slug()).orElseGet(Challenge::create);
            if (challenge.getCreatedAt() == null) {
                challenge.setCreatedAt(now);
            }
            challenge.setSlug(item.slug());
            challenge.setSortOrder(item.sortOrder());
            challenge.setTitle(item.title());
            challenge.setDifficulty(item.difficulty());
            challenge.setShortDescription(item.shortDescription());
            challenge.setStatement(item.statementMd());
            challenge.setStarterCode(item.starterCode());
            challenge.setTestsJson(item.testsJson());
            challenge.setMvpAvailable(item.availableInMvp());
            challengeRepository.save(challenge);
        }
    }
}
