# Supabase Migration Notes

Apply migrations in timestamp order.

1. `20260216_profiles_comments_auth_refactor.sql`
2. `20260224_add_notification_retention_tables.sql`
3. `20260225_cleanup_notification_state_on_profile_soft_delete.sql`
4. `20260226_enable_comments_blocks_realtime_publication.sql`
5. `20260227_add_notice_dispatch_state_tables.sql`

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

`20260225_cleanup_notification_state_on_profile_soft_delete.sql` introduces:
- soft-delete(`profiles.is_deleted=true`) 전환 시 `notification_subscriptions`/`user_interest_profiles` 정리 트리거
- 기존 soft-delete 계정의 잔존 notification/interest row 일괄 정리

`20260226_enable_comments_blocks_realtime_publication.sql` introduces:
- Realtime publication(`supabase_realtime`)에 `comments`, `blocks` 테이블을 보장
- 댓글/차단 화면의 실시간 동기화 구독 실패 방지

`20260227_add_notice_dispatch_state_tables.sql` introduces:
- `notification_dispatch_state` table (new notice dispatch run state)
- `notification_seen_notices` table (minimal dedupe keys with TTL)
- `notification_dispatch_state.updated_at` trigger

Smoke test:
- `python3 supabase/scripts/notification_rls_smoke_test.py`

## Edge Functions

Android currently calls these Supabase Edge Functions:

- `banned_until`
- `delete_user`

If those functions exist, update them to be compatible with soft-delete (`profiles.is_deleted`, `profiles.deleted_at`).

Before releasing account-related features, deploy both functions to the linked project:

```bash
supabase functions deploy banned_until --no-verify-jwt
supabase functions deploy delete_user --no-verify-jwt
```

`banned_until`, `delete_user`는 `verify_jwt=false`를 기본 정책으로 유지합니다.
둘 다 함수 내부에서 `Authorization` Bearer를 추출한 뒤 `adminClient.auth.getUser(bearer)`로 인증을 검증합니다.
이 패턴은 Edge gateway JWT 검증 단계와 앱 세션 JWT 알고리즘/키 설정이 어긋나는 상황을 회피할 수 있습니다.

- 클라이언트 호출(`supabase.functions.invoke("delete_user", ...)`)은 로그인 세션 JWT가 있을 때만 유효합니다.
- body 필드는 예시이며, 성공/실패를 가르는 핵심은 Authorization Bearer에 사용자 세션 토큰이 포함되는지입니다.

For notification retention rollout, deploy:
- `new_notice_dispatch`
- `notification_token_cleanup`

Smoke tests:
- `python3 supabase/scripts/notification_rls_smoke_test.py`
- `python3 supabase/scripts/new_notice_dispatch_smoke_test.py`
- `python3 supabase/scripts/notification_token_cleanup_smoke_test.py`

## New Notice Dispatch Rollout Checklist

1. DB migration 적용
- `supabase db push --linked`
- `notification_dispatch_state`, `notification_seen_notices` 생성 확인

2. 함수 배포
- `supabase functions deploy new_notice_dispatch`
- 환경 변수 확인:
  - `SUPABASE_URL`
  - `SUPABASE_SERVICE_ROLE_KEY`
  - `PUBLIC_PET_API_SERVICE_KEY`
  - `FIREBASE_PROJECT_ID`
  - `FIREBASE_SERVICE_ACCOUNT_JSON`

3. 검증
- `python3 supabase/scripts/notification_rls_smoke_test.py`
- `python3 supabase/scripts/new_notice_dispatch_smoke_test.py`
- `./gradlew :app:compileDevDebugKotlin --no-daemon`

4. 스케줄 확인
- GitHub Actions `new-notice-dispatch` 워크플로우 활성화
- `production` environment secret 주입 여부 확인

상세 운영 절차는 `docs/new-notice-dispatch-runbook.md` 참고.
