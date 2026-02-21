create or replace function public.sync_profile_ban_from_auth_users()
returns trigger
language plpgsql
security definer
set search_path = public, auth
as $$
begin
  update public.profiles
  set banned_until = new.banned_until
  where user_id = new.id;

  return new;
end;
$$;

drop trigger if exists trg_sync_profile_ban_from_auth_users on auth.users;

create trigger trg_sync_profile_ban_from_auth_users
after update of banned_until on auth.users
for each row
when (old.banned_until is distinct from new.banned_until)
execute function public.sync_profile_ban_from_auth_users();
