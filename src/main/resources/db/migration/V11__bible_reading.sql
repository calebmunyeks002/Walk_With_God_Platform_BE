-- ============================================================
-- V11 — Bible reading progress + highlights
-- ============================================================

create table if not exists bible_read_chapters (
    id uuid primary key,
    created_at timestamptz not null,
    user_id uuid not null references users(id) on delete cascade,
    version varchar(20) not null,
    book varchar(40) not null,
    chapter integer not null,
    read_at timestamptz not null default now(),
    constraint uk_read_chapter_user unique (user_id, book, chapter)
);

create index if not exists idx_read_user
    on bible_read_chapters(user_id, read_at desc);
create index if not exists idx_read_user_book
    on bible_read_chapters(user_id, book);

create table if not exists bible_highlights (
    id uuid primary key,
    created_at timestamptz not null,
    user_id uuid not null references users(id) on delete cascade,
    version varchar(20) not null,
    book varchar(40) not null,
    chapter integer not null,
    verse integer not null,
    color varchar(20) not null default 'yellow',
    note varchar(1000),
    constraint uk_highlight_user_verse unique (user_id, version, book, chapter, verse)
);

create index if not exists idx_highlight_user
    on bible_highlights(user_id, created_at desc);
create index if not exists idx_highlight_user_chapter
    on bible_highlights(user_id, book, chapter);