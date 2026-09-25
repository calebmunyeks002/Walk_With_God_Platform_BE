-- ============================================================
-- V5 — Devotions: admin-managed fields
-- ============================================================

alter table devotions add column featured boolean not null default false;
alter table devotions add column hidden boolean not null default false;
alter table devotions add column hidden_reason varchar(500);
alter table devotions add column hidden_at timestamptz;
alter table devotions add column hidden_by uuid references users(id);
alter table devotions add column updated_at timestamptz;

create index idx_devotions_featured on devotions(featured);
create index idx_devotions_hidden on devotions(hidden);
create index if not exists idx_devotions_published on devotions(published, published_at desc);
create index if not exists idx_devotions_featured on devotions(featured);
create index if not exists idx_devotions_hidden on devotions(hidden);