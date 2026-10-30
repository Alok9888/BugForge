package com.reactcode.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;

@Entity
@Table(
        name = "challenge_progress",
        uniqueConstraints = @UniqueConstraint(name = "uq_progress_user_challenge", columnNames = {"user_id", "challenge_id"})
)
public class ChallengeProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "challenge_id", nullable = false)
    private Challenge challenge;

    @Column(nullable = false)
    private boolean solved;

    @Column(name = "solved_at")
    private Instant solvedAt;

    @Column(name = "last_submitted_code", columnDefinition = "TEXT")
    private String lastSubmittedCode;

    @Column(name = "attempt_count", nullable = false)
    private int attemptCount;

    @Column(name = "last_passed_count")
    private Integer lastPassedCount;

    @Column(name = "last_failed_count")
    private Integer lastFailedCount;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected ChallengeProgress() {
    }

    public ChallengeProgress(AppUser user, Challenge challenge) {
        this.user = user;
        this.challenge = challenge;
        this.solved = false;
        this.attemptCount = 0;
        this.updatedAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public AppUser getUser() {
        return user;
    }

    public Challenge getChallenge() {
        return challenge;
    }

    public boolean isSolved() {
        return solved;
    }

    public Instant getSolvedAt() {
        return solvedAt;
    }

    public String getLastSubmittedCode() {
        return lastSubmittedCode;
    }

    public int getAttemptCount() {
        return attemptCount;
    }

    public Integer getLastPassedCount() {
        return lastPassedCount;
    }

    public Integer getLastFailedCount() {
        return lastFailedCount;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void recordAttempt(String code, int passed, int failed, boolean allPassed, Instant now) {
        this.attemptCount += 1;
        this.lastSubmittedCode = code;
        this.lastPassedCount = passed;
        this.lastFailedCount = failed;
        this.updatedAt = now;
        if (allPassed && !this.solved) {
            this.solved = true;
            this.solvedAt = now;
        }
    }
}
