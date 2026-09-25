-- ============================================================
-- V6 — Trivia: admin-managed fields (soft delete)
-- ============================================================

alter table trivia_questions add column hidden boolean not null default false;
alter table trivia_questions add column hidden_reason varchar(500);
alter table trivia_questions add column hidden_at timestamptz;
alter table trivia_questions add column hidden_by uuid references users(id);
alter table trivia_questions add column updated_at timestamptz;

create index if not exists idx_trivia_hidden on trivia_questions(hidden);
create index if not exists idx_trivia_difficulty on trivia_questions(difficulty);