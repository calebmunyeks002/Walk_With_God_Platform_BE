-- ============================================================
-- V8 — Platform settings (key/value store)
-- ============================================================

create table if not exists platform_settings (
    id uuid primary key,
    created_at timestamptz not null,
    updated_at timestamptz,
    key varchar(80) not null unique,
    value text,
    value_type varchar(20) not null default 'STRING',
    category varchar(40) not null default 'GENERAL',
    description varchar(500),
    updated_by uuid references users(id)
);

create index if not exists idx_settings_category on platform_settings(category);
create index if not exists idx_settings_key on platform_settings(key);