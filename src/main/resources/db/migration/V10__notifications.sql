-- ============================================================
-- V10 — Notifications (in-app + email)
-- ============================================================

create table if not exists notifications (
    id uuid primary key,
    created_at timestamptz not null,
    recipient_id uuid not null references users(id) on delete cascade,
    sender_id uuid references users(id) on delete set null,
    type varchar(40) not null,
    title varchar(200) not null,
    body varchar(2000),
    link varchar(500),
    read boolean not null default false,
    read_at timestamptz,
    email_sent boolean not null default false,
    metadata varchar(2000)
);

create index if not exists idx_notifications_recipient
    on notifications(recipient_id, created_at desc);
create index if not exists idx_notifications_unread
    on notifications(recipient_id, read);
create index if not exists idx_notifications_type
    on notifications(type);
create index if not exists idx_notifications_created
    on notifications(created_at desc);