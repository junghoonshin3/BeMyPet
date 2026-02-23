do $$
begin
  if to_regclass('public.notification_subscriptions') is not null then
    delete from public.notification_subscriptions ns
    using public.profiles p
    where ns.user_id = p.user_id
      and p.is_deleted = true;
  end if;

  if to_regclass('public.user_interest_profiles') is not null then
    delete from public.user_interest_profiles uip
    using public.profiles p
    where uip.user_id = p.user_id
      and p.is_deleted = true;
  end if;
end;
$$;

create or replace function public.cleanup_notification_state_on_profile_soft_delete()
returns trigger
language plpgsql
security definer
set search_path = public
as $$
begin
  if new.is_deleted = true and coalesce(old.is_deleted, false) = false then
    if to_regclass('public.notification_subscriptions') is not null then
      delete from public.notification_subscriptions
      where user_id = new.user_id;
    end if;

    if to_regclass('public.user_interest_profiles') is not null then
      delete from public.user_interest_profiles
      where user_id = new.user_id;
    end if;
  end if;

  return new;
end;
$$;

drop trigger if exists trg_profiles_cleanup_notification_state on public.profiles;
create trigger trg_profiles_cleanup_notification_state
after update of is_deleted on public.profiles
for each row
when (new.is_deleted is distinct from old.is_deleted)
execute function public.cleanup_notification_state_on_profile_soft_delete();
