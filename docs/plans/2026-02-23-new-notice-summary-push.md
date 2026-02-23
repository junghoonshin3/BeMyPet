# 신규 공고 요약 푸시(6시간 주기) Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** 공공 API 기반 신규 유기동물 공고를 6시간마다 감지해서, 관심 조건에 맞는 사용자에게 요약 푸시 1건을 안정적으로 발송한다.

**Architecture:** GitHub Actions가 6시간마다 Supabase Edge Function(`new_notice_dispatch`)을 호출한다. Edge Function은 날짜 단위(`bgupd/enupd`)로 공공 API를 겹침 조회하고, `notification_seen_notices`로 dedupe한 신규 공고만 대상으로 관심사 매칭/FCM 발송/전송 로그 기록을 수행한다. 공고 원문은 저장하지 않고 dispatch 상태와 dedupe 키만 저장한다.

**Tech Stack:** Supabase Postgres + RLS, Supabase Edge Functions(Deno), Firebase Cloud Messaging HTTP v1, GitHub Actions cron, Python smoke test scripts

---

### Task 1: Dispatch 상태/신규키 테이블 마이그레이션 추가

**Files:**
- Create: `supabase/migrations/20260227_add_notice_dispatch_state_tables.sql`
- Modify: `supabase/migrations/README.md`
- Test: `supabase/scripts/notification_rls_smoke_test.py`

**Step 1: 마이그레이션 스키마 테스트 먼저 작성(실패 기준 설정)**

`notification_rls_smoke_test.py`에 아래 검증을 먼저 추가한다.

```python
# state/seen 테이블이 존재해야 한다
for table in ["notification_dispatch_state", "notification_seen_notices"]:
    status, body = http_json(
        "GET",
        rest_url(f"/rest/v1/{table}?select=*&limit=1"),
        headers={
            "apikey": service_key,
            "Authorization": f"Bearer {service_key}",
            "Accept": "application/json",
        },
    )
    assert status == 200, f"missing table: {table}"
```

**Step 2: 테스트 실행(실패 확인)**

Run: `python3 supabase/scripts/notification_rls_smoke_test.py`  
Expected: 새 테이블 미존재로 FAIL

**Step 3: 최소 마이그레이션 구현**

`20260227_add_notice_dispatch_state_tables.sql`에 아래를 구현한다.

```sql
create table if not exists public.notification_dispatch_state (
  id smallint primary key default 1 check (id = 1),
  last_success_date date,
  last_run_started_at timestamptz,
  last_run_completed_at timestamptz,
  last_error_at timestamptz,
  last_error_message text,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);

create table if not exists public.notification_seen_notices (
  notice_key text primary key,
  notice_no text,
  desertion_no text,
  source_updated_date date,
  first_seen_at timestamptz not null default now(),
  expires_at timestamptz not null default (now() + interval '30 days')
);

create index if not exists notification_seen_notices_expires_at_idx
  on public.notification_seen_notices (expires_at);

alter table public.notification_dispatch_state enable row level security;
alter table public.notification_seen_notices enable row level security;
```

`migrations/README.md`에 신규 migration 설명/적용 순서를 추가한다.

**Step 4: 테스트 재실행(통과 확인)**

Run: `python3 supabase/scripts/notification_rls_smoke_test.py`  
Expected: PASS

**Step 5: 커밋**

```bash
git add supabase/migrations/20260227_add_notice_dispatch_state_tables.sql supabase/migrations/README.md supabase/scripts/notification_rls_smoke_test.py
git commit -m "feat(db): add dispatch state and seen notice tables"
```

### Task 2: 신규 감지/매칭 코어 로직을 순수 함수로 분리하고 단위 테스트 추가

**Files:**
- Create: `supabase/functions/new_notice_dispatch/dispatch_core.ts`
- Create: `supabase/functions/new_notice_dispatch/dispatch_core_test.ts`
- Modify: `supabase/functions/new_notice_dispatch/index.ts`

**Step 1: 실패하는 단위 테스트 작성**

`dispatch_core_test.ts`에 최소 4개 테스트를 작성한다.

```ts
Deno.test("buildDateWindow applies one-day overlap", () => {
  const w = buildDateWindow("2026-02-22", "2026-02-23");
  if (w.bgupd !== "20260221" || w.enupd !== "20260223") throw new Error("window mismatch");
});

Deno.test("buildNoticeKey prefers noticeNo over desertionNo", () => {
  const key = buildNoticeKey({ noticeNo: "N-1", desertionNo: "D-1" });
  if (key !== "N-1") throw new Error("key mismatch");
});

Deno.test("matchInterest returns true when profile field is empty", () => {
  const ok = matchesInterest({ regions: [], species: [] }, { uprCd: "6110000", upkind: "417000" });
  if (!ok) throw new Error("should match wildcard");
});

Deno.test("summarizeByUser groups many notices into one payload", () => {
  const rows = summarizeByUser([
    { userId: "u1", noticeKey: "n1" },
    { userId: "u1", noticeKey: "n2" },
  ]);
  if (rows[0].matchedCount !== 2) throw new Error("grouping failed");
});
```

**Step 2: 테스트 실행(실패 확인)**

Run: `deno test supabase/functions/new_notice_dispatch/dispatch_core_test.ts`  
Expected: FAIL (`buildDateWindow is not defined` 등)

**Step 3: 최소 구현 작성**

`dispatch_core.ts`에 테스트를 통과시키는 최소 함수들을 구현한다.

```ts
export function buildDateWindow(lastSuccessDate: string | null, todayIso: string) { /* ... */ }
export function buildNoticeKey(n: { noticeNo?: string; desertionNo?: string }) { /* ... */ }
export function matchesInterest(profile: InterestProfile, notice: NoticeCandidate) { /* ... */ }
export function summarizeByUser(rows: UserNoticeMatch[]) { /* ... */ }
```

`index.ts`에서 해당 함수를 import하도록 시작점만 연결한다.

**Step 4: 테스트 재실행(통과 확인)**

Run: `deno test supabase/functions/new_notice_dispatch/dispatch_core_test.ts`  
Expected: PASS

**Step 5: 커밋**

```bash
git add supabase/functions/new_notice_dispatch/dispatch_core.ts supabase/functions/new_notice_dispatch/dispatch_core_test.ts supabase/functions/new_notice_dispatch/index.ts
git commit -m "test(functions): add dispatch core unit tests and helpers"
```

### Task 3: FCM HTTP v1 발송 모듈 추가(토큰 발급 포함)

**Files:**
- Create: `supabase/functions/new_notice_dispatch/fcm_client.ts`
- Create: `supabase/functions/new_notice_dispatch/fcm_client_test.ts`
- Modify: `supabase/functions/new_notice_dispatch/index.ts`

**Step 1: 실패 테스트 작성**

```ts
Deno.test("buildAccessTokenRequest uses service account scope", () => {
  const req = buildAccessTokenRequest("{...service account json...}");
  if (!req.scope.includes("firebase.messaging")) throw new Error("scope missing");
});

Deno.test("buildSummaryMessage maps payload fields", () => {
  const msg = buildSummaryMessage("token", "3", "batch-1");
  if (msg.message.data.matched_count !== "3") throw new Error("payload mismatch");
});
```

**Step 2: 테스트 실행(실패 확인)**

Run: `deno test supabase/functions/new_notice_dispatch/fcm_client_test.ts`  
Expected: FAIL

**Step 3: 최소 구현 작성**

`fcm_client.ts`에 아래 인터페이스를 구현한다.

```ts
export async function getAccessToken(serviceAccountJson: string): Promise<string> { /* oauth2 token */ }
export async function sendSummaryMessage(input: SendSummaryInput): Promise<SendResult> { /* POST /v1/projects/{project}/messages:send */ }
export function classifyFcmError(errorBody: unknown): "invalid_token" | "retryable" | "fatal" { /* ... */ }
```

`index.ts`에서 env 필수값 검증을 추가한다.
- `FIREBASE_PROJECT_ID`
- `FIREBASE_SERVICE_ACCOUNT_JSON`

**Step 4: 테스트 재실행(통과 확인)**

Run: `deno test supabase/functions/new_notice_dispatch/fcm_client_test.ts`  
Expected: PASS

**Step 5: 커밋**

```bash
git add supabase/functions/new_notice_dispatch/fcm_client.ts supabase/functions/new_notice_dispatch/fcm_client_test.ts supabase/functions/new_notice_dispatch/index.ts
git commit -m "feat(functions): add fcm v1 client for summary push"
```

### Task 4: `new_notice_dispatch`를 실제 발송 파이프라인으로 완성

**Files:**
- Modify: `supabase/functions/new_notice_dispatch/index.ts`
- Modify: `supabase/functions/new_notice_dispatch/README.md`
- Test: `supabase/scripts/new_notice_dispatch_smoke_test.py`

**Step 1: 실패하는 스모크 테스트 확장**

`new_notice_dispatch_smoke_test.py`에 응답 필드 검증을 먼저 추가한다.

```python
assert "window" in body
assert "new_notice_count" in body
assert "sent_count" in body
assert "failed_count" in body
```

**Step 2: 테스트 실행(실패 확인)**

Run: `python3 supabase/scripts/new_notice_dispatch_smoke_test.py`  
Expected: FAIL (필드 미존재)

**Step 3: 함수 본 구현**

`index.ts`를 아래 순서로 구현한다.

1. `dispatch_state` 조회 및 `last_run_started_at` 갱신
2. 날짜 윈도우 계산(`bgupd/enupd`)
3. 공공 API 페이지네이션 조회(최대 페이지/레코드 상한 포함)
4. 신규 키 선별(`notification_seen_notices`와 비교 후 insert)
5. 사용자 관심사 + 구독 조회 후 매칭
6. 사용자별 요약 메시지 1건 발송
7. `notification_delivery_logs` 기록(`sent`/`failed`)
8. 무효 토큰 삭제
9. 성공 시 `last_success_date`/`last_run_completed_at` 갱신, 실패 시 `last_error_*` 기록

**Step 4: 스모크 테스트 재실행(통과 확인)**

Run: `python3 supabase/scripts/new_notice_dispatch_smoke_test.py`  
Expected: PASS (`matched_users`, `sent_count` 등 출력)

**Step 5: 커밋**

```bash
git add supabase/functions/new_notice_dispatch/index.ts supabase/functions/new_notice_dispatch/README.md supabase/scripts/new_notice_dispatch_smoke_test.py
git commit -m "feat(functions): implement 6h new notice summary dispatch"
```

### Task 5: 6시간 스케줄 워크플로우 추가

**Files:**
- Create: `.github/workflows/new-notice-dispatch.yml`
- Modify: `docs/notification-token-cleanup-runbook.md`

**Step 1: 워크플로우 유효성 체크(실패 케이스 먼저 정의)**

로컬에서 YAML lint(가능 시) 또는 `gh workflow` 조회로 파일 인식 확인.

Run: `gh workflow view new-notice-dispatch.yml --repo junghoonshin3/BeMyPet`  
Expected: 초기에는 Not Found

**Step 2: 워크플로우 작성**

```yaml
name: new-notice-dispatch
on:
  schedule:
    - cron: "0 */6 * * *"
  workflow_dispatch: {}
jobs:
  dispatch:
    runs-on: ubuntu-latest
    environment: production
    steps:
      - name: Invoke new_notice_dispatch
        env:
          SUPABASE_URL: ${{ secrets.SUPABASE_URL }}
          SUPABASE_SERVICE_ROLE_KEY: ${{ secrets.SUPABASE_SERVICE_ROLE_KEY }}
        run: |
          curl -sS -X POST "${SUPABASE_URL}/functions/v1/new_notice_dispatch" \
            -H "apikey: ${SUPABASE_SERVICE_ROLE_KEY}" \
            -H "Authorization: Bearer ${SUPABASE_SERVICE_ROLE_KEY}" \
            -H "Content-Type: application/json" \
            --data '{"dry_run":false}'
```

**Step 3: 인식 확인**

Run: `gh workflow view new-notice-dispatch.yml --repo junghoonshin3/BeMyPet`  
Expected: workflow metadata 출력

**Step 4: 커밋**

```bash
git add .github/workflows/new-notice-dispatch.yml docs/notification-token-cleanup-runbook.md
git commit -m "chore(ci): schedule new notice summary dispatch every 6 hours"
```

### Task 6: 앱 수신 표시/분석 이벤트 최소 연동

**Files:**
- Modify: `app/src/main/java/kr/sjh/bemypet/notifications/BeMyPetFirebaseMessagingService.kt`
- Create: `app/src/main/java/kr/sjh/bemypet/notifications/PushNotificationPresenter.kt`
- Test: `app/src/test/java/kr/sjh/bemypet/notifications/PushPayloadParserTest.kt`

**Step 1: 실패 테스트 작성**

```kotlin
@Test
fun parser_reads_summary_payload_fields() {
    val parsed = PushPayloadParser.parse(
        mapOf("campaign_type" to "new_animal_summary", "matched_count" to "4")
    )
    assertEquals("new_animal_summary", parsed.campaignType)
}
```

**Step 2: 테스트 실행(실패 확인)**

Run: `./gradlew :app:testDebugUnitTest --tests "*PushPayloadParserTest" --no-daemon`  
Expected: FAIL (필드/테스트 부재)

**Step 3: 최소 구현 작성**

- `PushNotificationPresenter`에서 포그라운드 수신 시 로컬 알림 표시
- `BeMyPetFirebaseMessagingService`에서 `push_received` Firebase event 기록
- payload의 `campaign_type`, `matched_count`, `batch_id` 전달

**Step 4: 테스트 재실행(통과 확인)**

Run: `./gradlew :app:testDebugUnitTest --tests "*PushPayloadParserTest" --no-daemon`  
Expected: PASS

**Step 5: 커밋**

```bash
git add app/src/main/java/kr/sjh/bemypet/notifications/BeMyPetFirebaseMessagingService.kt app/src/main/java/kr/sjh/bemypet/notifications/PushNotificationPresenter.kt app/src/test/java/kr/sjh/bemypet/notifications/PushPayloadParserTest.kt
git commit -m "feat(app): show summary push in foreground and log received event"
```

### Task 7: 통합 검증 및 배포 체크리스트 확정

**Files:**
- Modify: `supabase/migrations/README.md`
- Modify: `supabase/functions/new_notice_dispatch/README.md`
- Create: `docs/new-notice-dispatch-runbook.md`

**Step 1: 통합 검증 명령 실행**

```bash
python3 supabase/scripts/new_notice_dispatch_smoke_test.py
python3 supabase/scripts/notification_rls_smoke_test.py
./gradlew :app:compileDevDebugKotlin --no-daemon
```

Expected: all PASS

**Step 2: 함수/워크플로우 배포 절차 문서화**

- `supabase functions deploy new_notice_dispatch`
- GitHub Environment `production` secrets 확인
- `workflow_dispatch` 수동 1회 실행 후 로그 점검

**Step 3: 최종 커밋**

```bash
git add supabase/migrations/README.md supabase/functions/new_notice_dispatch/README.md docs/new-notice-dispatch-runbook.md
git commit -m "docs(ops): add new notice dispatch runbook and verification checklist"
```

### 최종 PR 체크리스트

- [ ] DB migration 적용됨 (`notification_dispatch_state`, `notification_seen_notices`)
- [ ] `new_notice_dispatch`가 실제 FCM 발송/실패 처리/중복 방지를 수행함
- [ ] 6시간 cron 워크플로우가 production 환경에서 실행됨
- [ ] 앱에서 summary push 수신 시 foreground 알림 확인됨
- [ ] Firebase 이벤트(`push_received`)와 서버 로그(`sent/failed`)가 함께 확인됨
