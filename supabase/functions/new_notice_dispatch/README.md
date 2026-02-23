# new_notice_dispatch

6시간 주기 신규 공고 요약 푸시 발송 함수입니다.

- `notification_dispatch_state`를 기준으로 날짜 윈도우(`bgupd/enupd`)를 계산합니다.
- 공공 API(또는 요청 payload의 notices)에서 공고를 수집합니다.
- `notification_seen_notices`로 신규 공고만 dedupe합니다.
- 푸시 수신 동의 사용자 전체에게 사용자별 요약 푸시 1건을 발송합니다.
- `notification_delivery_logs`에 `sent`/`failed`를 기록하고, 무효 토큰을 정리합니다.

## Request

`POST /functions/v1/new_notice_dispatch`

```json
{
  "dry_run": true,
  "notices": [{ "notice_no": "A2026-0001" }],
  "max_pages": 5,
  "num_of_rows": 100
}
```

## Response

```json
{
  "dry_run": true,
  "window": { "bgupd": "20260222", "enupd": "20260223" },
  "fetched_notice_count": 42,
  "new_notice_count": 7,
  "matched_users": 4,
  "target_token_count": 5,
  "sent_count": 0,
  "failed_count": 0,
  "invalid_token_deleted_count": 0
}
```

- `matched_users`: 이번 배치에서 실제 발송 대상이 된 사용자 수

## Required Environment Variables

- `SUPABASE_URL`
- `SUPABASE_SERVICE_ROLE_KEY`
- `PUBLIC_PET_API_SERVICE_KEY` (payload `notices`를 사용하지 않고 공공 API 조회 시 필요)

실발송(`dry_run=false`) 시 추가:

- `FIREBASE_PROJECT_ID`
- `FIREBASE_SERVICE_ACCOUNT_JSON`

옵션:

- `PUBLIC_PET_API_BASE_URL` (기본값: `https://apis.data.go.kr/1543061/abandonmentPublicService_v2/`)

## Deploy

```bash
supabase functions deploy new_notice_dispatch
```

## Scheduler

- GitHub Actions: `.github/workflows/new-notice-dispatch.yml`
- 주기: 6시간 (`cron: 0 */6 * * *`, `production` environment 고정)
- 수동 실행: `production` 또는 `development` environment 선택 가능
- 요청: `POST /functions/v1/new_notice_dispatch`
  - schedule: `{"dry_run": false}`
  - manual: `{"dry_run": <input>}`
- 호출은 `curl --fail-with-body`로 실행되어 HTTP 4xx/5xx를 즉시 실패 처리

## Quick Verification

1. 함수 배포 후 스모크 테스트 실행

```bash
python3 supabase/scripts/new_notice_dispatch_smoke_test.py
```

2. 워크플로우 수동 실행 후 Actions 로그 확인

```bash
gh workflow run new-notice-dispatch.yml --repo junghoonshin3/BeMyPet
```

3. 응답/로그 확인 포인트
- `new_notice_count`
- `matched_users`
- `sent_count`
- `failed_count`
- `invalid_token_deleted_count`

## Troubleshooting

- `401/403`: `SUPABASE_SERVICE_ROLE_KEY` 또는 environment secret 설정 오류
- `500`: `PUBLIC_PET_API_SERVICE_KEY`, `FIREBASE_*` 누락/형식 오류
- `sent_count=0`:
  - 신규 공고(`new_notice_count`) 자체가 없음
  - `notification_subscriptions.push_opt_in=true` 대상이 없음
  - 구독은 있으나 `fcm_token`이 비어 있거나 무효 토큰 정리로 대상이 사라짐
