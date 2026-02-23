# new_notice_dispatch

6시간 주기 신규 공고 요약 푸시 발송 함수입니다.

- `notification_dispatch_state`를 기준으로 날짜 윈도우(`bgupd/enupd`)를 계산합니다.
- 공공 API(또는 요청 payload의 notices)에서 공고를 수집합니다.
- `notification_seen_notices`로 신규 공고만 dedupe합니다.
- 관심사/구독 매칭 후 사용자별 요약 푸시 1건을 발송합니다.
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
