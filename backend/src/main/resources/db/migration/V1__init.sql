CREATE TABLE app_users (
    id UUID PRIMARY KEY,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE challenges (
    id BIGSERIAL PRIMARY KEY,
    slug VARCHAR(64) NOT NULL UNIQUE,
    sort_order INTEGER NOT NULL UNIQUE,
    title VARCHAR(200) NOT NULL,
    difficulty VARCHAR(16) NOT NULL,
    short_description VARCHAR(500) NOT NULL,
    statement TEXT NOT NULL,
    starter_code TEXT NOT NULL,
    tests_json TEXT NOT NULL,
    mvp_available BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE submissions (
    id BIGSERIAL PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES app_users (id),
    challenge_id BIGINT NOT NULL REFERENCES challenges (id),
    code TEXT NOT NULL,
    status VARCHAR(16) NOT NULL,
    passed_count INTEGER NOT NULL,
    failed_count INTEGER NOT NULL,
    total_count INTEGER NOT NULL,
    results_json TEXT,
    verification_source VARCHAR(32) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_submissions_user_challenge ON submissions (user_id, challenge_id, created_at DESC);

CREATE TABLE challenge_progress (
    id BIGSERIAL PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES app_users (id),
    challenge_id BIGINT NOT NULL REFERENCES challenges (id),
    solved BOOLEAN NOT NULL DEFAULT FALSE,
    solved_at TIMESTAMPTZ,
    last_submitted_code TEXT,
    attempt_count INTEGER NOT NULL DEFAULT 0,
    last_passed_count INTEGER,
    last_failed_count INTEGER,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_progress_user_challenge UNIQUE (user_id, challenge_id)
);
