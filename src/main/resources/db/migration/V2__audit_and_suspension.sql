-- ============================================================
-- V2 — Audit trail and user suspension fields
-- ============================================================

-- Add suspension fields to users
alter table users add column suspended boolean not null default false;
alter table users add column suspension_reason varchar(500);
alter table users add column suspended_at timestamptz;
alter table users add column suspended_by uuid references users(id);

create index idx_users_suspended on users(suspended);

-- Audit trail table
create table audit_logs (
    id uuid primary key,
    created_at timestamptz not null,
    actor_id uuid references users(id),
    actor_name varchar(120),
    actor_role varchar(20),
    action varchar(80) not null,
    target_type varchar(60),
    target_id uuid,
    reason varchar(500),
    ip_address varchar(45),
    user_agent varchar(400),
    metadata jsonb,
    success boolean not null default true
);

create index idx_audit_created on audit_logs(created_at desc);
create index idx_audit_actor on audit_logs(actor_id);
create index idx_audit_action on audit_logs(action);
create index idx_audit_target on audit_logs(target_type, target_id);