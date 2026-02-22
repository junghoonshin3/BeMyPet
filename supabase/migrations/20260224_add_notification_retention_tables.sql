create table if not exists public.user_interest_profiles (
  user_id uuid primary key references public.profiles(user_id) on delete cascade,
  regions text[] not null default '{}',
  species text[] not null default '{}',
  sexes text[] not null default '{}',
  sizes text[] not null default '{}',
  push_enabled boolean not null default true,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);

create table if not exists public.notification_subscriptions (
  id uuid primary key default gen_random_uuid(),
  user_id uuid not null references public.profiles(user_id) on delete cascade,
  fcm_token text not null,
  push_opt_in boolean not null default true,
  last_active_at timestamptz,
  last_sent_at timestamptz,
  daily_sent_count integer not null default 0 check (daily_sent_count >= 0),
  timezone text not null default 'Asia/Seoul',
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now(),
  unique (fcm_token)
);

create table if not exists public.notification_delivery_logs (
  id uuid primary key default gen_random_uuid(),
  user_id uuid not null references public.profiles(user_id) on delete cascade,
  campaign_type text not null check (campaign_type in ('new_animal', 'daily_digest', 'revisit_nudge')),
  notice_no text,
  dedupe_key text not null unique,
  status text not null check (status in ('queued', 'sent', 'failed', 'opened')),
  payload_json jsonb not null default '{}'::jsonb,
  sent_at timestamptz,
  opened_at timestamptz,
  created_at timestamptz not null default now()
);

create index if not exists notification_subscriptions_user_id_idx
  on public.notification_subscriptions (user_id);

create index if not exists notification_subscriptions_user_push_idx
  on public.notification_subscriptions (user_id, push_opt_in);

create index if not exists notification_delivery_logs_user_campaign_created_idx
  on public.notification_delivery_logs (user_id, campaign_type, created_at desc);

create index if not exists notification_delivery_logs_notice_created_idx
  on public.notification_delivery_logs (notice_no, created_at desc);

drop trigger if exists trg_user_interest_profiles_updated_at on public.user_interest_profiles;
create trigger trg_user_interest_profiles_updated_at
before update on public.user_interest_profiles
for each row
execute function public.set_updated_at();

drop trigger if exists trg_notification_subscriptions_updated_at on public.notification_subscriptions;
create trigger trg_notification_subscriptions_updated_at
before update on public.notification_subscriptions
for each row
execute function public.set_updated_at();

alter table public.user_interest_profiles enable row level security;
alter table public.notification_subscriptions enable row level security;
alter table public.notification_delivery_logs enable row level security;

drop policy if exists user_interest_profiles_select_self on public.user_interest_profiles;
create policy user_interest_profiles_select_self
on public.user_interest_profiles
for select
using (auth.uid() = user_id);

drop policy if exists user_interest_profiles_insert_self on public.user_interest_profiles;
create policy user_interest_profiles_insert_self
on public.user_interest_profiles
for insert
with check (auth.uid() = user_id);

drop policy if exists user_interest_profiles_update_self on public.user_interest_profiles;
create policy user_interest_profiles_update_self
on public.user_interest_profiles
for update
using (auth.uid() = user_id)
with check (auth.uid() = user_id);

drop policy if exists user_interest_profiles_delete_self on public.user_interest_profiles;
create policy user_interest_profiles_delete_self
on public.user_interest_profiles
for delete
using (auth.uid() = user_id);

drop policy if exists notification_subscriptions_select_self on public.notification_subscriptions;
create policy notification_subscriptions_select_self
on public.notification_subscriptions
for select
using (auth.uid() = user_id);

drop policy if exists notification_subscriptions_insert_self on public.notification_subscriptions;
create policy notification_subscriptions_insert_self
on public.notification_subscriptions
for insert
with check (auth.uid() = user_id);

drop policy if exists notification_subscriptions_update_self on public.notification_subscriptions;
create policy notification_subscriptions_update_self
on public.notification_subscriptions
for update
using (auth.uid() = user_id)
with check (auth.uid() = user_id);

drop policy if exists notification_subscriptions_delete_self on public.notification_subscriptions;
create policy notification_subscriptions_delete_self
on public.notification_subscriptions
for delete
using (auth.uid() = user_id);

drop policy if exists notification_delivery_logs_select_self on public.notification_delivery_logs;
create policy notification_delivery_logs_select_self
on public.notification_delivery_logs
for select
using (auth.uid() = user_id);
