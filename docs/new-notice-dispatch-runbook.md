# New Notice Dispatch Runbook

## 목적

- 신규 유기동물 공고를 6시간 주기로 감지하고, 관심사 매칭 사용자에게 요약 푸시를 발송한다.
- 공고 원문 적재 없이 최소 상태(`dispatch_state`, `seen_notice`)만 저장해 비용을 통제한다.

## 구성

- Edge Function: `new_notice_dispatch`
- Workflow: `.github/workflows/new-notice-dispatch.yml`
- 주기: 6시간 (`cron: 0 */6 * * *`)
- 저장 테이블:
  - `notification_dispatch_state`
  - `notification_seen_notices`
  - `notification_delivery_logs` (전송 기록)

## 선행 조건

1. Supabase migration 적용 완료
- `20260227_add_notice_dispatch_state_tables.sql`

2. 필수 환경 변수 설정
- Supabase Function/Actions 공통
  - `SUPABASE_URL`
  - `SUPABASE_SERVICE_ROLE_KEY`
- Function 런타임
  - `PUBLIC_PET_API_SERVICE_KEY`
  - `FIREBASE_PROJECT_ID`
  - `FIREBASE_SERVICE_ACCOUNT_JSON`

3. GitHub Actions
- `production` environment 생성
- 해당 environment에 `SUPABASE_URL`, `SUPABASE_SERVICE_ROLE_KEY` 등록

## 배포 절차

1. 함수 배포

```bash
supabase functions deploy new_notice_dispatch
```

2. 스모크 테스트

```bash
python3 supabase/scripts/new_notice_dispatch_smoke_test.py
python3 supabase/scripts/notification_rls_smoke_test.py
```

3. 앱 컴파일 검증

```bash
./gradlew :app:compileDevDebugKotlin --no-daemon
```

4. 워크플로우 수동 1회 실행

```bash
gh workflow run new-notice-dispatch.yml --repo junghoonshin3/BeMyPet
```

## 정상 동작 기준

- Edge Function 응답이 HTTP 200
- 응답에 아래 필드 포함
  - `window`
  - `new_notice_count`
  - `matched_users`
  - `sent_count`
  - `failed_count`
  - `invalid_token_deleted_count`
- `failed_count`가 비정상적으로 치솟지 않음

## 운영 점검 포인트

- `new_notice_count`는 있는데 `matched_users=0`
  - 관심사 프로필 미설정/매칭 조건 과도 가능성 확인
- `matched_users`는 있는데 `sent_count=0`
  - 구독 토큰(`notification_subscriptions`) 상태 확인
  - `push_opt_in=true` 여부 확인
- `failed_count` 증가
  - Firebase 키/권한/프로젝트 ID 검증
  - 실패 코드가 invalid token이면 정리 로직 동작 여부 확인

## 장애 대응

1. 즉시 dry-run 수동 호출

```bash
curl -sS -X POST "${SUPABASE_URL}/functions/v1/new_notice_dispatch" \
  -H "apikey: ${SUPABASE_SERVICE_ROLE_KEY}" \
  -H "Authorization: Bearer ${SUPABASE_SERVICE_ROLE_KEY}" \
  -H "Content-Type: application/json" \
  --data '{"dry_run":true}'
```

2. 환경 변수/시크릿 확인
3. Function 재배포 후 smoke test 재실행
4. 필요 시 workflow를 일시 비활성화하고 원인 해결 후 재개

## 참고

- 함수 문서: `supabase/functions/new_notice_dispatch/README.md`
- 스케줄 워크플로우: `.github/workflows/new-notice-dispatch.yml`
- 토큰 정리 런북: `docs/notification-token-cleanup-runbook.md`
