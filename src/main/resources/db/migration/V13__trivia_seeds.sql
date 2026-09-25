-- ============================================================
-- V13 — Trivia question templates (seed data for the generator)
-- ============================================================

create table if not exists trivia_seeds (
    id uuid primary key,
    created_at timestamptz not null,
    type varchar(30) not null,          -- VERSE_COMPLETE | VERSE_SOURCE | VERSE_SPEAKER | LIFE_APPLICATION
    difficulty varchar(10) not null,    -- EASY | MEDIUM | HARD
    prompt varchar(1000) not null,      -- question template
    correct_answer varchar(500) not null,
    wrong_answers varchar(1000) not null, -- comma-separated wrong options
    explanation varchar(1000),
    source_ref varchar(200),            -- e.g. "John 3:16" or "Matthew 6:14"
    active boolean not null default true
);

create index if not exists idx_seeds_type_diff on trivia_seeds(type, difficulty, active);