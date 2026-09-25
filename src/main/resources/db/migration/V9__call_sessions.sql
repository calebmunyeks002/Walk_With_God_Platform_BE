-- ============================================================
-- V9 — Call sessions (WebRTC audio/video call history)
-- ============================================================

create table if not exists call_sessions (
    id uuid primary key,
    created_at timestamptz not null,
    caller_id uuid not null references users(id),
    callee_id uuid not null references users(id),
    status varchar(20) not null default 'RINGING',
    media_type varchar(10) not null default 'VIDEO',
    started_at timestamptz,
    answered_at timestamptz,
    ended_at timestamptz,
    duration_seconds integer,
    ended_by uuid references users(id),
    end_reason varchar(40)
);

create index if not exists idx_calls_caller on call_sessions(caller_id, created_at desc);
create index if not exists idx_calls_callee on call_sessions(callee_id, created_at desc);
create index if not exists idx_calls_status on call_sessions(status);