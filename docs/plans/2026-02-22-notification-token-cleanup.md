# Notification Token Cleanup Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** 사용하지 않는 FCM 토큰 row를 `notification_subscriptions`에서 자동 삭제한다(영구 실패 토큰 즉시 삭제 훅 + 30일 비활성 배치 삭제).

**Architecture:** `notification_token_cleanup` Edge Function을 추가해 `stale`/`invalid` 두 모드를 제공한다. `stale` 모드는 `last_active_at`(없으면 `updated_at`, `created_at`) 기준 30일 초과 토큰을 삭제하고, `invalid` 모드는 전달받은 토큰 목록을 즉시 삭제한다. 인증은 service role 호출만 허용하고, `dry_run`으로 안전 검증 후 운영 전환한다.

**Tech Stack:** Supabase Edge Functions (Deno + supabase-js), PostgREST, Python smoke tests, GitHub Actions cron

---

### Task 1: Add Token Cleanup Edge Function + Smoke Test

**Files:**
- Create: `supabase/functions/notification_token_cleanup/index.ts`
- Create: `supabase/functions/notification_token_cleanup/README.md`
- Create: `supabase/scripts/notification_token_cleanup_smoke_test.py`
- Modify: `supabase/migrations/README.md`

**Step 1: Write the failing smoke test**

```python
# supabase/scripts/notification_token_cleanup_smoke_test.py
status, body = http_json(
    "POST",
    rest_url("/functions/v1/notification_token_cleanup"),
    headers={"apikey": service_key, "Authorization": f"Bearer {service_key}"},
    payload={"mode": "stale", "stale_before_days": 30, "dry_run": True},
)
assert status == 200, f"expected deployed function, got {status} {body}"
```

**Step 2: Run test to verify it fails**

Run: `python3 supabase/scripts/notification_token_cleanup_smoke_test.py`  
Expected: FAIL with `Function not found` (404).

**Step 3: Write minimal implementation**

```ts
// supabase/functions/notification_token_cleanup/index.ts
import { createClient } from "jsr:@supabase/supabase-js@2";

type CleanupMode = "stale" | "invalid";
type CleanupRequest = {
  mode?: CleanupMode;
  stale_before_days?: number;
  invalid_tokens?: string[];
  dry_run?: boolean;
};

function json(body: unknown, status = 200) {
  return new Response(JSON.stringify(body), {
    status,
    headers: { "Content-Type": "application/json" },
  });
}

function extractBearerToken(authHeader: string | null): string {
  if (!authHeader || !authHeader.startsWith("Bearer ")) return "";
  return authHeader.substring(7).trim();
}

function parseJwtRole(token: string): string | null {
  try {
    const payloadPart = token.split(".")[1];
    if (!payloadPart) return null;
    const b64 = payloadPart.replace(/-/g, "+").replace(/_/g, "/");
    const padded = b64 + "=".repeat((4 - (b64.length % 4)) % 4);
    const payload = JSON.parse(atob(padded));
    return typeof payload?.role === "string" ? payload.role : null;
  } catch {
    return null;
  }
}

function parseIsoMs(value: string | null | undefined): number {
  if (!value) return Number.NaN;
  const ts = Date.parse(value);
  return Number.isFinite(ts) ? ts : Number.NaN;
}

Deno.serve(async (req) => {
  if (req.method !== "POST") return json({ error: "Method not allowed" }, 405);

  const supabaseUrl = Deno.env.get("SUPABASE_URL");
  const serviceRoleKey = Deno.env.get("SUPABASE_SERVICE_ROLE_KEY");
  if (!supabaseUrl || !serviceRoleKey) return json({ error: "Missing server configuration" }, 500);

  const bearer = extractBearerToken(req.headers.get("Authorization"));
  if (!bearer) return json({ error: "Unauthorized" }, 401);
  const isServiceInvoker = bearer === serviceRoleKey || parseJwtRole(bearer) === "service_role";
  if (!isServiceInvoker) return json({ error: "Unauthorized" }, 401);

  let payload: CleanupRequest = {};
  try {
    payload = await req.json();
  } catch {
    payload = {};
  }

  const mode = payload.mode;
  const dryRun = payload.dry_run ?? true;
  const staleBeforeDays = Math.max(1, payload.stale_before_days ?? 30);
  const admin = createClient(supabaseUrl, serviceRoleKey, {
    auth: { autoRefreshToken: false, persistSession: false },
  });

  if (mode === "stale") {
    const cutoffMs = Date.now() - staleBeforeDays * 24 * 60 * 60 * 1000;
    const { data, error } = await admin
      .from("notification_subscriptions")
      .select("id,fcm_token,last_active_at,updated_at,created_at");
    if (error) return json({ error: "Failed to query subscriptions", details: error.message }, 500);

    const staleRows = (data ?? []).filter((row) => {
      const lastSeenMs =
        parseIsoMs(row.last_active_at) || parseIsoMs(row.updated_at) || parseIsoMs(row.created_at);
      return Number.isFinite(lastSeenMs) && lastSeenMs <= cutoffMs;
    });

    if (!dryRun && staleRows.length > 0) {
      const ids = staleRows.map((row) => row.id);
      const { error: deleteError } = await admin
        .from("notification_subscriptions")
        .delete()
        .in("id", ids);
      if (deleteError) return json({ error: "Failed to delete stale subscriptions", details: deleteError.message }, 500);
    }

    return json({
      mode: "stale",
      stale_before_days: staleBeforeDays,
      dry_run: dryRun,
      matched_count: staleRows.length,
      deleted_count: dryRun ? 0 : staleRows.length,
    });
  }

  if (mode === "invalid") {
    const tokens = Array.from(new Set((payload.invalid_tokens ?? []).map((x) => x.trim()).filter((x) => x.length > 0)));
    if (tokens.length === 0) return json({ error: "invalid_tokens required for invalid mode" }, 400);

    const { data, error } = await admin
      .from("notification_subscriptions")
      .select("id,fcm_token")
      .in("fcm_token", tokens);
    if (error) return json({ error: "Failed to query invalid token rows", details: error.message }, 500);

    const matchedRows = data ?? [];
    if (!dryRun && matchedRows.length > 0) {
      const ids = matchedRows.map((row) => row.id);
      const { error: deleteError } = await admin
        .from("notification_subscriptions")
        .delete()
        .in("id", ids);
      if (deleteError) return json({ error: "Failed to delete invalid token rows", details: deleteError.message }, 500);
    }

    return json({
      mode: "invalid",
      dry_run: dryRun,
      matched_count: matchedRows.length,
      deleted_count: dryRun ? 0 : matchedRows.length,
    });
  }

  return json({ error: "mode must be one of: stale, invalid" }, 400);
});
```

**Step 4: Deploy function and run smoke test**

Run:
1. `supabase functions deploy notification_token_cleanup`
2. `python3 supabase/scripts/notification_token_cleanup_smoke_test.py`

Expected:
- stale dry-run PASS (`deleted_count=0`)
- stale delete PASS (`deleted_count>=1` for seeded stale rows)
- invalid dry-run/delete PASS

**Step 5: Commit**

```bash
git add supabase/functions/notification_token_cleanup/index.ts supabase/functions/notification_token_cleanup/README.md supabase/scripts/notification_token_cleanup_smoke_test.py supabase/migrations/README.md
git commit -m "feat(functions): add notification token cleanup edge function"
```

### Task 2: Add Daily Stale Cleanup Scheduler + Runbook

**Files:**
- Create: `.github/workflows/notification-token-cleanup.yml`
- Create: `docs/notification-token-cleanup-runbook.md`
- Modify: `docs/index.md`

**Step 1: Write failing check for missing scheduler workflow**

```bash
test -f .github/workflows/notification-token-cleanup.yml
```

**Step 2: Run check to verify it fails**

Run: `test -f .github/workflows/notification-token-cleanup.yml`  
Expected: non-zero exit status (file missing).

**Step 3: Write minimal workflow + runbook**

```yaml
# .github/workflows/notification-token-cleanup.yml
name: notification-token-cleanup

on:
  schedule:
    - cron: "0 16 * * *" # 01:00 KST
  workflow_dispatch: {}

jobs:
  cleanup:
    runs-on: ubuntu-latest
    steps:
      - name: Invoke stale token cleanup (30 days)
        env:
          SUPABASE_URL: ${{ secrets.SUPABASE_URL }}
          SUPABASE_SERVICE_ROLE_KEY: ${{ secrets.SUPABASE_SERVICE_ROLE_KEY }}
        run: |
          curl -sS -X POST "${SUPABASE_URL}/functions/v1/notification_token_cleanup" \
            -H "apikey: ${SUPABASE_SERVICE_ROLE_KEY}" \
            -H "Authorization: Bearer ${SUPABASE_SERVICE_ROLE_KEY}" \
            -H "Content-Type: application/json" \
            --data '{"mode":"stale","stale_before_days":30,"dry_run":false}'
```

**Step 4: Verify docs/workflow references**

Run:
1. `rg -n "notification_token_cleanup|stale_before_days|workflow_dispatch" .github/workflows/notification-token-cleanup.yml`
2. `rg -n "notification-token-cleanup|SUPABASE_SERVICE_ROLE_KEY|30일" docs/notification-token-cleanup-runbook.md docs/index.md`

Expected: all patterns found.

**Step 5: Commit**

```bash
git add .github/workflows/notification-token-cleanup.yml docs/notification-token-cleanup-runbook.md docs/index.md
git commit -m "chore(ops): schedule daily stale token cleanup"
```

## Done Criteria

- `notification_token_cleanup` 함수가 `stale`/`invalid` 두 모드를 지원한다.
- `stale_before_days=30` 기준으로 row 삭제가 동작한다.
- `dry_run`에서 실제 삭제 없이 건수만 반환한다.
- smoke test가 stale/invalid 케이스를 모두 PASS 한다.
- 스케줄러가 일 1회 stale cleanup을 자동 실행한다.

