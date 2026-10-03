-- ============================================================
-- V17 — Mentor exclusivity
-- ============================================================

-- Track when a request was accepted
ALTER TABLE mentor_requests
    ADD COLUMN IF NOT EXISTS accepted_at TIMESTAMPTZ NULL;

ALTER TABLE mentor_requests
    ADD COLUMN IF NOT EXISTS declined_at TIMESTAMPTZ NULL;

ALTER TABLE mentor_requests
    ADD COLUMN IF NOT EXISTS ended_at TIMESTAMPTZ NULL;

ALTER TABLE mentor_requests
    ADD COLUMN IF NOT EXISTS end_reason VARCHAR(500) NULL;

-- ------------------------------------------------------------
-- Clean up existing duplicate (member_id, mentor_id) pairs
-- Keeps the most recent request per pair; deletes older ones.
-- ------------------------------------------------------------
DELETE FROM mentor_requests
WHERE id IN (
    SELECT id FROM (
        SELECT id,
               ROW_NUMBER() OVER (
                   PARTITION BY member_id, mentor_id
                   ORDER BY created_at DESC
               ) AS rn
        FROM mentor_requests
    ) sub
    WHERE rn > 1
);

-- ------------------------------------------------------------
-- Now the unique constraint will apply cleanly.
-- ------------------------------------------------------------
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'uk_mentor_request_pair'
    ) THEN
        ALTER TABLE mentor_requests
            ADD CONSTRAINT uk_mentor_request_pair
            UNIQUE (member_id, mentor_id);
    END IF;
END $$;

-- Speed up "does this member already have an active request?" queries
CREATE INDEX IF NOT EXISTS idx_mentor_requests_member_status
    ON mentor_requests(member_id, status);

CREATE INDEX IF NOT EXISTS idx_mentor_requests_mentor_status
    ON mentor_requests(mentor_id, status);