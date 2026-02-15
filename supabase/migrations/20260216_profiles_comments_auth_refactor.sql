create extension if not exists pgcrypto;

create table if not exists public.profiles (
  user_id uuid primary key references auth.users(id) on delete cascade,
  display_name text not null,
  avatar_url text,
  is_deleted boolean not null default false,
  deleted_at timestamptz,
  nickname_changed_at timestamptz,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);

create unique index if not exists profiles_display_name_unique_idx on public.profiles (lower(display_name));

create table if not exists public.user_roles (
  user_id uuid primary key references auth.users(id) on delete cascade,
  role text not null default 'user' check (role in ('user', 'admin')),
  created_at timestamptz not null default now()
);

create table if not exists public.comments (
  id uuid primary key default gen_random_uuid(),
  notice_no text not null,
  user_id uuid not null references public.profiles(user_id),
  content text not null,
  created_at timestamptz not null default now(),
  updated_at timestamptz,
  deleted_at timestamptz
);

create table if not exists public.blocks (
  blocker_id uuid not null references public.profiles(user_id),
  blocked_id uuid not null references public.profiles(user_id),
  created_at timestamptz not null default now(),
  primary key (blocker_id, blocked_id)
);

create table if not exists public.reports (
  id uuid primary key default gen_random_uuid(),
  type text not null,
  reported_by uuid not null,
  reported_user uuid not null,
  comment_id uuid,
  reason text not null,
  description text not null default '',
  created_at timestamptz not null default now()
);

do $$
begin
  if exists (
    select 1
    from information_schema.columns
    where table_schema = 'public'
      and table_name = 'comments'
      and column_name = 'post_id'
  ) and not exists (
    select 1
    from information_schema.columns
    where table_schema = 'public'
      and table_name = 'comments'
      and column_name = 'notice_no'
  ) then
    alter table public.comments rename column post_id to notice_no;
  end if;

  if exists (
    select 1
    from information_schema.columns
    where table_schema = 'public'
      and table_name = 'comments'
      and column_name = 'notice_no'
      and data_type <> 'text'
  ) then
    alter table public.comments alter column notice_no type text using notice_no::text;
  end if;

  if not exists (
    select 1
    from information_schema.columns
    where table_schema = 'public'
      and table_name = 'comments'
      and column_name = 'updated_at'
  ) then
    alter table public.comments add column updated_at timestamptz;
  end if;

  if not exists (
    select 1
    from information_schema.columns
    where table_schema = 'public'
      and table_name = 'comments'
      and column_name = 'deleted_at'
  ) then
    alter table public.comments add column deleted_at timestamptz;
  end if;

  if exists (
    select 1
    from information_schema.columns
    where table_schema = 'public'
      and table_name = 'blocks'
      and column_name = 'raw_user_meta_data'
  ) then
    alter table public.blocks drop column raw_user_meta_data;
  end if;

  if not exists (
    select 1
    from information_schema.columns
    where table_schema = 'public'
      and table_name = 'blocks'
      and column_name = 'created_at'
  ) then
    alter table public.blocks add column created_at timestamptz not null default now();
  end if;

  if exists (
    select 1
    from information_schema.columns
    where table_schema = 'public'
      and table_name = 'blocks'
      and column_name = 'blocker_id'
      and data_type <> 'uuid'
  ) then
    alter table public.blocks alter column blocker_id type uuid using blocker_id::uuid;
  end if;

  if exists (
    select 1
    from information_schema.columns
    where table_schema = 'public'
      and table_name = 'blocks'
      and column_name = 'blocked_id'
      and data_type <> 'uuid'
  ) then
    alter table public.blocks alter column blocked_id type uuid using blocked_id::uuid;
  end if;
end
$$;

create index if not exists comments_notice_no_created_at_idx on public.comments (notice_no, created_at desc);
create index if not exists comments_user_id_created_at_idx on public.comments (user_id, created_at desc);
create index if not exists blocks_blocker_idx on public.blocks (blocker_id);
create index if not exists reports_reported_by_created_at_idx on public.reports (reported_by, created_at desc);
create index if not exists reports_reported_user_created_at_idx on public.reports (reported_user, created_at desc);
create index if not exists reports_comment_id_idx on public.reports (comment_id);

alter table public.comments
  drop constraint if exists comments_user_id_fkey,
  add constraint comments_user_id_fkey
    foreign key (user_id) references public.profiles(user_id) on delete cascade;

alter table public.blocks
  drop constraint if exists blocks_blocker_id_fkey,
  add constraint blocks_blocker_id_fkey
    foreign key (blocker_id) references public.profiles(user_id) on delete cascade,
  drop constraint if exists blocks_blocked_id_fkey,
  add constraint blocks_blocked_id_fkey
    foreign key (blocked_id) references public.profiles(user_id) on delete cascade;

alter table if exists public.reports
  drop constraint if exists reports_reported_by_fkey,
  add constraint reports_reported_by_fkey
    foreign key (reported_by) references public.profiles(user_id),
  drop constraint if exists reports_reported_user_fkey,
  add constraint reports_reported_user_fkey
    foreign key (reported_user) references public.profiles(user_id),
  drop constraint if exists reports_comment_id_fkey,
  add constraint reports_comment_id_fkey
    foreign key (comment_id) references public.comments(id);

create or replace function public.is_admin(check_user_id uuid)
returns boolean
language sql
stable
security definer
set search_path = public
as $$
  select exists (
    select 1
    from public.user_roles
    where user_id = check_user_id
      and lower(role) = 'admin'
  );
$$;

create or replace function public.set_updated_at()
returns trigger
language plpgsql
as $$
begin
  new.updated_at = now();
  return new;
end;
$$;

create or replace function public.enforce_nickname_cooldown()
returns trigger
language plpgsql
as $$
begin
  if new.display_name is distinct from old.display_name then
    if old.nickname_changed_at is not null and old.nickname_changed_at > now() - interval '30 days' then
      raise exception 'Nickname can only be changed once every 30 days';
    end if;
    new.nickname_changed_at = now();
  end if;
  return new;
end;
$$;

create or replace function public.on_auth_user_created()
returns trigger
language plpgsql
security definer
set search_path = public
as $$
begin
  insert into public.profiles(user_id, display_name, avatar_url)
  values (
    new.id,
    coalesce(new.raw_user_meta_data ->> 'name', new.raw_user_meta_data ->> 'full_name', 'user_' || right(new.id::text, 6)) || '_' || right(new.id::text, 6),
    new.raw_user_meta_data ->> 'avatar_url'
  )
  on conflict (user_id) do nothing;
  return new;
end;
$$;

drop trigger if exists trg_profiles_updated_at on public.profiles;
create trigger trg_profiles_updated_at
before update on public.profiles
for each row
execute function public.set_updated_at();

drop trigger if exists trg_comments_updated_at on public.comments;
create trigger trg_comments_updated_at
before update on public.comments
for each row
execute function public.set_updated_at();

drop trigger if exists trg_profiles_nickname_cooldown on public.profiles;
create trigger trg_profiles_nickname_cooldown
before update of display_name on public.profiles
for each row
execute function public.enforce_nickname_cooldown();

drop trigger if exists on_auth_user_created on auth.users;
create trigger on_auth_user_created
after insert on auth.users
for each row
execute function public.on_auth_user_created();

insert into public.profiles (user_id, display_name, avatar_url)
select
  au.id,
  coalesce(au.raw_user_meta_data ->> 'name', au.raw_user_meta_data ->> 'full_name', 'user_' || right(au.id::text, 6)) || '_' || right(au.id::text, 6),
  au.raw_user_meta_data ->> 'avatar_url'
from auth.users au
on conflict (user_id) do nothing;

create or replace view public.comment_feed with (security_invoker = true) as
select
  c.id,
  c.notice_no,
  c.user_id,
  c.content,
  c.created_at,
  c.updated_at,
  p.display_name as author_name,
  p.avatar_url as author_avatar_url,
  p.is_deleted as author_deleted
from public.comments c
join public.profiles p
  on p.user_id = c.user_id
where c.deleted_at is null;

create or replace view public.block_feed with (security_invoker = true) as
select
  b.blocker_id,
  b.blocked_id,
  b.created_at,
  p.display_name as blocked_name,
  p.avatar_url as blocked_avatar_url
from public.blocks b
join public.profiles p
  on p.user_id = b.blocked_id
where p.is_deleted = false;

alter table public.profiles enable row level security;
alter table public.comments enable row level security;
alter table public.blocks enable row level security;
alter table if exists public.reports enable row level security;

drop policy if exists profiles_select_active on public.profiles;
create policy profiles_select_active
on public.profiles
for select
using (auth.role() = 'authenticated' and is_deleted = false);

drop policy if exists profiles_update_self on public.profiles;
create policy profiles_update_self
on public.profiles
for update
using (auth.uid() = user_id)
with check (auth.uid() = user_id and is_deleted = false);

drop policy if exists comments_select_visible on public.comments;
create policy comments_select_visible
on public.comments
for select
using (
  auth.role() = 'authenticated'
  and deleted_at is null
  and not exists (
    select 1
    from public.blocks b
    where b.blocker_id = auth.uid()
      and b.blocked_id = comments.user_id
  )
);

drop policy if exists comments_insert_self on public.comments;
create policy comments_insert_self
on public.comments
for insert
with check (
  auth.role() = 'authenticated'
  and auth.uid() = user_id
  and notice_no is not null
  and btrim(content) <> ''
);

drop policy if exists comments_update_self_or_admin on public.comments;
create policy comments_update_self_or_admin
on public.comments
for update
using (
  auth.uid() = user_id
  or public.is_admin(auth.uid())
)
with check (
  auth.uid() = user_id
  or public.is_admin(auth.uid())
);

drop policy if exists comments_delete_self_or_admin on public.comments;
create policy comments_delete_self_or_admin
on public.comments
for delete
using (
  auth.uid() = user_id
  or public.is_admin(auth.uid())
);

drop policy if exists blocks_select_self on public.blocks;
create policy blocks_select_self
on public.blocks
for select
using (auth.uid() = blocker_id);

drop policy if exists blocks_insert_self on public.blocks;
create policy blocks_insert_self
on public.blocks
for insert
with check (auth.uid() = blocker_id);

drop policy if exists blocks_delete_self on public.blocks;
create policy blocks_delete_self
on public.blocks
for delete
using (auth.uid() = blocker_id);

drop policy if exists reports_insert_authenticated on public.reports;
create policy reports_insert_authenticated
on public.reports
for insert
with check (auth.role() = 'authenticated' and auth.uid() = reported_by);

insert into storage.buckets (id, name, public)
values ('profile-images', 'profile-images', true)
on conflict (id) do update set public = excluded.public;

drop policy if exists "profile-images public read" on storage.objects;
create policy "profile-images public read"
on storage.objects
for select
using (bucket_id = 'profile-images');

drop policy if exists "profile-images upload own folder" on storage.objects;
create policy "profile-images upload own folder"
on storage.objects
for insert
with check (
  bucket_id = 'profile-images'
  and split_part(name, '/', 1) = auth.uid()::text
);

drop policy if exists "profile-images update own folder" on storage.objects;
create policy "profile-images update own folder"
on storage.objects
for update
using (
  bucket_id = 'profile-images'
  and split_part(name, '/', 1) = auth.uid()::text
)
with check (
  bucket_id = 'profile-images'
  and split_part(name, '/', 1) = auth.uid()::text
);

drop policy if exists "profile-images delete own folder" on storage.objects;
create policy "profile-images delete own folder"
on storage.objects
for delete
using (
  bucket_id = 'profile-images'
  and split_part(name, '/', 1) = auth.uid()::text
);
