-- ============================================================
-- V4 — Content moderation: soft-delete posts, reports table
-- ============================================================

-- Soft-delete support for posts
alter table posts add column hidden boolean not null default false;
alter table posts add column hidden_reason varchar(500);
alter table posts add column hidden_at timestamptz;
alter table posts add column hidden_by uuid references users(id);

create index idx_posts_hidden on posts(hidden);

-- Soft-delete support for comments
alter table comments add column hidden boolean not null default false;
alter table comments add column hidden_reason varchar(500);
alter table comments add column hidden_at timestamptz;
alter table comments add column hidden_by uuid references users(id);

create index idx_comments_hidden on comments(hidden);

-- Reports table (polymorphic — can target post, comment or user)
create table reports (
    id uuid primary key,
    created_at timestamptz not null,
    reporter_id uuid not null references users(id),
    target_type varchar(20) not null,
    target_id uuid not null,
    reason varchar(80) not null,
    details varchar(2000),
    status varchar(20) not null default 'OPEN',
    resolved_by uuid references users(id),
    resolved_at timestamptz,
    resolution_note varchar(1000)
);

create index idx_reports_status on reports(status);
create index idx_reports_target on reports(target_type, target_id);
create index idx_reports_reporter on reports(reporter_id);
create index idx_reports_created on reports(created_at desc);