package com.reactcode.repo;

import com.reactcode.domain.Challenge;
import com.reactcode.domain.Difficulty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface ChallengeRepository extends JpaRepository<Challenge, Long>, JpaSpecificationExecutor<Challenge> {
    Optional<Challenge> findBySlug(String slug);

    long countByMvpAvailableTrue();
}
