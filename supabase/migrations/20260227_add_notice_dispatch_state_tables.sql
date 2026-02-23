create table if not exists public.notification_dispatch_state (
  id smallint primary key default 1 check (id = 1),
  last_success_date date,
  last_run_started_at timestamptz,
  last_run_completed_at timestamptz,
  last_error_at timestamptz,
  last_error_message text,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);

create table if not exists public.notification_seen_notices (
  notice_key text primary key,
  notice_no text,
  desertion_no text,
  source_updated_date date,
  first_seen_at timestamptz not null default now(),
  expires_at timestamptz not null default (now() + interval '30 days')
);

create index if not exists notification_seen_notices_expires_at_idx
  on public.notification_seen_notices (expires_at);

drop trigger if exists trg_notification_dispatch_state_updated_at on public.notification_dispatch_state;
create trigger trg_notification_dispatch_state_updated_at
before update on public.notification_dispatch_state
for each row
execute function public.set_updated_at();

alter table public.notification_dispatch_state enable row level security;
alter table public.notification_seen_notices enable row level security;
