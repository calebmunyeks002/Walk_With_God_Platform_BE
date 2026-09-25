-- ============================================================
-- V7 — Mentor requests: lifecycle timestamps
-- ============================================================

alter table mentor_requests add column responded_at timestamptz;
alter table mentor_requests add column response_note varchar(1000);
alter table mentor_requests add column completed_at timestamptz;

create index if not exists idx_mr_mentor_status
    on mentor_requests(mentor_id, status);
create index if not exists idx_mr_member_status
    on mentor_requests(member_id, status);