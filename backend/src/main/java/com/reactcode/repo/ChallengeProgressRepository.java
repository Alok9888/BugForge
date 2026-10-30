package com.reactcode.repo;

import com.reactcode.domain.AppUser;
import com.reactcode.domain.Challenge;
import com.reactcode.domain.ChallengeProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ChallengeProgressRepository extends JpaRepository<ChallengeProgress, Long> {
    Optional<ChallengeProgress> findByUserAndChallenge(AppUser user, Challenge challenge);

    @Query("select p from ChallengeProgress p join fetch p.challenge where p.user.id = :userId")
    List<ChallengeProgress> findByUserId(@Param("userId") UUID userId);

    long countByUserIdAndSolvedTrue(UUID userId);
}
