-- ============================================================
-- V16 — Batch 4: Communities
-- ============================================================

CREATE TABLE IF NOT EXISTS communities (
    id                UUID PRIMARY KEY,
    created_at        TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    name              VARCHAR(120)  NOT NULL,
    slug              VARCHAR(140)  NOT NULL,
    description       VARCHAR(2000),
    cover_image_url   VARCHAR(1000),
    icon_emoji        VARCHAR(20)   DEFAULT '🏛',

    creator_id        UUID          NOT NULL,
    visibility        VARCHAR(20)   NOT NULL DEFAULT 'PUBLIC',

    member_count      INTEGER       NOT NULL DEFAULT 0,
    post_count        INTEGER       NOT NULL DEFAULT 0,

    hidden            BOOLEAN       NOT NULL DEFAULT FALSE,
    hidden_reason     VARCHAR(500),
    hidden_at         TIMESTAMPTZ,
    hidden_by         UUID,

    CONSTRAINT fk_community_creator FOREIGN KEY (creator_id) REFERENCES users(id) ON DELETE RESTRICT,
    CONSTRAINT uk_community_slug    UNIQUE (slug),
    CONSTRAINT chk_community_vis    CHECK (visibility IN ('PUBLIC','PRIVATE'))
);

CREATE INDEX IF NOT EXISTS idx_community_slug       ON communities(slug);
CREATE INDEX IF NOT EXISTS idx_community_creator    ON communities(creator_id);
CREATE INDEX IF NOT EXISTS idx_community_hidden     ON communities(hidden);
CREATE INDEX IF NOT EXISTS idx_community_visibility ON communities(visibility);


CREATE TABLE IF NOT EXISTS community_members (
    id            UUID PRIMARY KEY,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    community_id  UUID NOT NULL,
    user_id       UUID NOT NULL,
    role          VARCHAR(20) NOT NULL DEFAULT 'MEMBER',
    joined_at     TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_cm_community FOREIGN KEY (community_id) REFERENCES communities(id) ON DELETE CASCADE,
    CONSTRAINT fk_cm_user      FOREIGN KEY (user_id)      REFERENCES users(id)        ON DELETE CASCADE,
    CONSTRAINT uk_cm_pair      UNIQUE (community_id, user_id),
    CONSTRAINT chk_cm_role     CHECK (role IN ('OWNER','MODERATOR','MEMBER'))
);

CREATE INDEX IF NOT EXISTS idx_cm_community ON community_members(community_id);
CREATE INDEX IF NOT EXISTS idx_cm_user      ON community_members(user_id);


CREATE TABLE IF NOT EXISTS community_join_requests (
    id             UUID PRIMARY KEY,
    created_at     TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    community_id   UUID NOT NULL,
    user_id        UUID NOT NULL,
    message        VARCHAR(500),
    status         VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    reviewed_by    UUID,
    reviewed_at    TIMESTAMPTZ,

    CONSTRAINT fk_cjr_community FOREIGN KEY (community_id) REFERENCES communities(id) ON DELETE CASCADE,
    CONSTRAINT fk_cjr_user      FOREIGN KEY (user_id)      REFERENCES users(id)        ON DELETE CASCADE,
    CONSTRAINT uk_cjr_open      UNIQUE (community_id, user_id),
    CONSTRAINT chk_cjr_status   CHECK (status IN ('PENDING','APPROVED','REJECTED'))
);

CREATE INDEX IF NOT EXISTS idx_cjr_community_status ON community_join_requests(community_id, status);


-- Link posts to communities (nullable — global posts have community_id = NULL)
ALTER TABLE posts
    ADD COLUMN IF NOT EXISTS community_id UUID NULL;

ALTER TABLE posts
    ADD CONSTRAINT fk_posts_community
        FOREIGN KEY (community_id) REFERENCES communities(id) ON DELETE SET NULL;

CREATE INDEX IF NOT EXISTS idx_posts_community ON posts(community_id);