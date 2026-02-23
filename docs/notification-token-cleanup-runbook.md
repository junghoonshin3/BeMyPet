# Notification Token Cleanup Runbook

## 목적

- `notification_subscriptions`에서 30일 이상 비활성 FCM 토큰 row를 매일 정리한다.
- 오래된 토큰 누적으로 인한 push 실패율 증가와 불필요한 저장소 사용을 줄인다.

## 실행 방식

- GitHub Actions 워크플로우: `.github/workflows/notification-token-cleanup.yml`
- 실행 주기: 매일 01:00 KST (`cron: 0 16 * * *`, UTC 기준)
- 호출 함수: `POST /functions/v1/notification_token_cleanup`
- 요청 payload:

```json
{
  "mode": "stale",
  "stale_before_days": 30,
  "dry_run": false
}
```

## 필수 GitHub Secrets

- `SUPABASE_URL`
- `SUPABASE_SERVICE_ROLE_KEY`

## 수동 실행

1. GitHub `Actions` 탭에서 `notification-token-cleanup` 워크플로우 선택
2. `Run workflow` 실행
3. 실행 로그에서 `deleted_count`와 오류 메시지 확인

## 점검 포인트

- 정상: HTTP 200 응답, `mode=stale`, `deleted_count >= 0`
- 비정상:
  - 401: service role 키/권한 문제
  - 500: Edge Function 내부 쿼리 실패
  - `SUPABASE_URL` 또는 `SUPABASE_SERVICE_ROLE_KEY` 미설정

## 장애 대응

1. `notification_token_cleanup` 함수가 배포되어 있는지 확인
2. GitHub Secrets 값 최신화 확인
3. 먼저 `dry_run=true`로 수동 호출해 대상 건수 확인
4. 정상 확인 후 `dry_run=false`로 다시 실행

## 참고

- 함수 구현: `supabase/functions/notification_token_cleanup/index.ts`
- 스모크 테스트: `python3 supabase/scripts/notification_token_cleanup_smoke_test.py`
- 신규 공고 요약 푸시 스케줄: `.github/workflows/new-notice-dispatch.yml` (6시간 주기)
