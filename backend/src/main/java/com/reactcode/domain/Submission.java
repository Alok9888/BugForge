package com.reactcode.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "submissions")
public class Submission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "challenge_id", nullable = false)
    private Challenge challenge;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private SubmissionStatus status;

    @Column(name = "passed_count", nullable = false)
    private int passedCount;

    @Column(name = "failed_count", nullable = false)
    private int failedCount;

    @Column(name = "total_count", nullable = false)
    private int totalCount;

    @Column(name = "results_json", columnDefinition = "TEXT")
    private String resultsJson;

    @Enumerated(EnumType.STRING)
    @Column(name = "verification_source", nullable = false, length = 32)
    private VerificationSource verificationSource;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected Submission() {
    }

    public Submission(
            AppUser user,
            Challenge challenge,
            String code,
            SubmissionStatus status,
            int passedCount,
            int failedCount,
            int totalCount,
            String resultsJson,
            VerificationSource verificationSource,
            Instant createdAt
    ) {
        this.user = user;
        this.challenge = challenge;
        this.code = code;
        this.status = status;
        this.passedCount = passedCount;
        this.failedCount = failedCount;
        this.totalCount = totalCount;
        this.resultsJson = resultsJson;
        this.verificationSource = verificationSource;
        this.createdAt = createdAt;
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

    public String getCode() {
        return code;
    }

    public SubmissionStatus getStatus() {
        return status;
    }

    public int getPassedCount() {
        return passedCount;
    }

    public int getFailedCount() {
        return failedCount;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public String getResultsJson() {
        return resultsJson;
    }

    public VerificationSource getVerificationSource() {
        return verificationSource;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
