alter table if exists public.profiles
  add column if not exists banned_until timestamptz;
