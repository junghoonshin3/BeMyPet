create or replace function public.upsert_my_notification_subscription(
  p_fcm_token text,
  p_push_opt_in boolean default true,
  p_timezone text default 'Asia/Seoul'
)
returns void
language plpgsql
security definer
set search_path = public
as $$
declare
  v_uid uuid;
  v_fcm_token text;
  v_timezone text;
begin
  v_uid := auth.uid();
  if v_uid is null then
    raise exception 'Unauthorized' using errcode = '42501';
  end if;

  v_fcm_token := nullif(trim(coalesce(p_fcm_token, '')), '');
  if v_fcm_token is null then
    raise exception 'fcm_token is required' using errcode = '22023';
  end if;

  v_timezone := nullif(trim(coalesce(p_timezone, '')), '');
  if v_timezone is null then
    v_timezone := 'Asia/Seoul';
  end if;

  insert into public.notification_subscriptions (
    user_id,
    fcm_token,
    push_opt_in,
    timezone,
    last_active_at
  )
  values (
    v_uid,
    v_fcm_token,
    coalesce(p_push_opt_in, true),
    v_timezone,
    now()
  )
  on conflict (fcm_token)
  do update set
    user_id = excluded.user_id,
    push_opt_in = excluded.push_opt_in,
    timezone = excluded.timezone,
    last_active_at = now(),
    updated_at = now();
end;
$$;

revoke all on function public.upsert_my_notification_subscription(text, boolean, text) from public;
grant execute on function public.upsert_my_notification_subscription(text, boolean, text) to authenticated;
