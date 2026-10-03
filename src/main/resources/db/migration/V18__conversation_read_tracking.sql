-- ============================================================
-- V18 — Conversation read tracking (unread badges)
-- ============================================================

CREATE TABLE IF NOT EXISTS conversation_reads (
    id              UUID PRIMARY KEY,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    conversation_id UUID NOT NULL,
    user_id         UUID NOT NULL,
    last_read_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_cr_conversation FOREIGN KEY (conversation_id)
        REFERENCES conversations(id) ON DELETE CASCADE,
    CONSTRAINT fk_cr_user FOREIGN KEY (user_id)
        REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT uk_cr_pair UNIQUE (conversation_id, user_id)
);

CREATE INDEX IF NOT EXISTS idx_cr_user ON conversation_reads(user_id);
CREATE INDEX IF NOT EXISTS idx_cr_conv ON conversation_reads(conversation_id);