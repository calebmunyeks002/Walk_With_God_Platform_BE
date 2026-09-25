-- ============================================================
-- V12 — Daily trivia sessions + attempts
-- ============================================================

-- Per-user, per-day, per-difficulty assignment of question ids
create table if not exists trivia_sessions (
    id uuid primary key,
    created_at timestamptz not null,
    user_id uuid not null references users(id) on delete cascade,
    quiz_date date not null,
    difficulty varchar(10) not null,
    question_ids varchar(2000) not null,   -- comma-separated UUIDs (5 questions)
    completed boolean not null default false,
    passed boolean not null default false,
    score integer not null default 0,
    total integer not null default 0,
    completed_at timestamptz,
    constraint uk_trivia_session_user_day_diff
        unique (user_id, quiz_date, difficulty)
);

create index if not exists idx_ts_user_date on trivia_sessions(user_id, quiz_date desc);
create index if not exists idx_ts_date on trivia_sessions(quiz_date desc);
create index if not exists idx_ts_date_completed on trivia_sessions(quiz_date, completed);