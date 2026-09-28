-- ============================================================
-- V14 — Batch 2: Social feed, follows, prayers, devotions
-- ============================================================

-- ---------- Posts: add type + share chain ----------
ALTER TABLE posts
    ADD COLUMN IF NOT EXISTS type VARCHAR(20) NOT NULL DEFAULT 'NORMAL';

ALTER TABLE posts
    ADD COLUMN IF NOT EXISTS shared_from_id UUID NULL;

ALTER TABLE posts
    ADD CONSTRAINT fk_posts_shared_from
        FOREIGN KEY (shared_from_id) REFERENCES posts(id) ON DELETE SET NULL;

CREATE INDEX IF NOT EXISTS idx_posts_type ON posts(type);

-- ---------- Reactions: enforce type whitelist at DB level ----------
ALTER TABLE reactions
    ADD CONSTRAINT chk_reactions_type
        CHECK (type IN ('LIKE', 'LOVE', 'AMEN', 'PRAY'));

-- Speeds up per-type counts
CREATE INDEX IF NOT EXISTS idx_reactions_post_type
    ON reactions(post_id, type);

-- ---------- Comments: add delete-tracking ----------
ALTER TABLE comments
    ADD COLUMN IF NOT EXISTS deleted BOOLEAN NOT NULL DEFAULT FALSE;

-- ---------- User follows ----------
CREATE TABLE IF NOT EXISTS user_follows (
    id UUID PRIMARY KEY,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    follower_id UUID NOT NULL,
    followed_id UUID NOT NULL,
    CONSTRAINT fk_follow_follower FOREIGN KEY (follower_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_follow_followed FOREIGN KEY (followed_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT uk_follow_pair UNIQUE (follower_id, followed_id),
    CONSTRAINT chk_follow_not_self CHECK (follower_id <> followed_id)
);

CREATE INDEX IF NOT EXISTS idx_follow_follower ON user_follows(follower_id);
CREATE INDEX IF NOT EXISTS idx_follow_followed ON user_follows(followed_id);

-- ---------- Prayers ----------
CREATE TABLE IF NOT EXISTS prayers (
    id UUID PRIMARY KEY,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    title VARCHAR(200) NOT NULL,
    body TEXT NOT NULL,
    scripture VARCHAR(200),
    slot VARCHAR(20) NOT NULL,                 -- MORNING | EVENING
    prayer_date DATE NOT NULL,
    system_generated BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT chk_prayer_slot CHECK (slot IN ('MORNING','EVENING')),
    CONSTRAINT uk_prayer_slot_date UNIQUE (slot, prayer_date)
);

CREATE INDEX IF NOT EXISTS idx_prayer_date ON prayers(prayer_date DESC);

-- ---------- Devotions: system-generated flag ----------
ALTER TABLE devotions
    ADD COLUMN IF NOT EXISTS system_generated BOOLEAN NOT NULL DEFAULT FALSE;

CREATE INDEX IF NOT EXISTS idx_devotion_system ON devotions(system_generated);