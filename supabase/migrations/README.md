# Supabase Migration Notes

Apply migrations in timestamp order.

1. `20260216_profiles_comments_auth_refactor.sql`
2. `20260224_add_notification_retention_tables.sql`

This migration introduces:
- `profiles` table as app profile source
- `comments.notice_no` standardization
- `comment_feed` / `block_feed` views
- RLS policies for profiles/comments/blocks/reports
- profile-image storage bucket and policies

`20260224_add_notification_retention_tables.sql` introduces:
- `user_interest_profiles` table (new user preference targeting)
- `notification_subscriptions` table (device token + opt-in + delivery throttle state)
- `notification_delivery_logs` table (dedupe + delivery/open audit)
- RLS policies for self-scoped read/write where appropriate

Smoke test:
- `python3 supabase/scripts/notification_rls_smoke_test.py`

## Edge Functions

Android currently calls these Supabase Edge Functions:

- `banned_until`
- `delete_user`

If those functions exist, update them to be compatible with soft-delete (`profiles.is_deleted`, `profiles.deleted_at`).

Before releasing account-related features, deploy both functions to the linked project:

```bash
supabase functions deploy banned_until
supabase functions deploy delete_user
```

`delete_user`는 `verify_jwt=true`를 기본 정책으로 유지해야 합니다.

- 재배포 시 `--no-verify-jwt`를 사용하지 않습니다.
- 클라이언트 호출(`supabase.functions.invoke("delete_user", ...)`)은 로그인 세션 JWT가 있을 때만 유효합니다.
- body 필드는 예시이며, 성공/실패를 가르는 핵심은 Authorization Bearer에 사용자 세션 토큰이 포함되는지입니다.

For notification retention rollout, deploy:
- `new_notice_dispatch`
- `notification_token_cleanup`

Smoke tests:
- `python3 supabase/scripts/notification_rls_smoke_test.py`
- `python3 supabase/scripts/new_notice_dispatch_smoke_test.py`
- `python3 supabase/scripts/notification_token_cleanup_smoke_test.py`
