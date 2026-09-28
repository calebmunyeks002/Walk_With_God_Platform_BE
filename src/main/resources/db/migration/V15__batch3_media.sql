-- ============================================================
-- V15 — Batch 3: Media (images + videos) with moderation
-- ============================================================

CREATE TABLE IF NOT EXISTS media (
    id                UUID PRIMARY KEY,
    created_at        TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    uploader_id       UUID NOT NULL,
    post_id           UUID NULL,                        -- linked when attached to a post

    type              VARCHAR(20)  NOT NULL,            -- IMAGE | VIDEO
    status            VARCHAR(20)  NOT NULL,            -- PENDING | APPROVED | FLAGGED | REJECTED | DELETED

    storage_key       VARCHAR(500) NOT NULL,            -- relative path on disk
    original_filename VARCHAR(255) NOT NULL,
    content_type      VARCHAR(100) NOT NULL,
    size_bytes        BIGINT       NOT NULL,

    width             INTEGER      NULL,                -- images
    height            INTEGER      NULL,                -- images

    duration_seconds  DOUBLE PRECISION NULL,            -- videos

    checksum_sha256   VARCHAR(64)  NOT NULL,            -- duplicate detection

    flagged_reason    VARCHAR(500) NULL,
    reviewed_by       UUID         NULL,
    reviewed_at       TIMESTAMPTZ  NULL,

    CONSTRAINT fk_media_uploader FOREIGN KEY (uploader_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_media_post     FOREIGN KEY (post_id)     REFERENCES posts(id) ON DELETE SET NULL,
    CONSTRAINT chk_media_type    CHECK (type   IN ('IMAGE','VIDEO')),
    CONSTRAINT chk_media_status  CHECK (status IN ('PENDING','APPROVED','FLAGGED','REJECTED','DELETED'))
);

CREATE INDEX IF NOT EXISTS idx_media_uploader ON media(uploader_id);
CREATE INDEX IF NOT EXISTS idx_media_post     ON media(post_id);
CREATE INDEX IF NOT EXISTS idx_media_status   ON media(status);
CREATE INDEX IF NOT EXISTS idx_media_checksum ON media(checksum_sha256);

-- Link posts to a media row (nullable — legacy posts keep imageUrl)
ALTER TABLE posts
    ADD COLUMN IF NOT EXISTS media_id UUID NULL;

ALTER TABLE posts
    ADD CONSTRAINT fk_posts_media
        FOREIGN KEY (media_id) REFERENCES media(id) ON DELETE SET NULL;

CREATE INDEX IF NOT EXISTS idx_posts_media ON posts(media_id);