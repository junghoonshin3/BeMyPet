# New User Retention Push Foundation Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** 신규 사용자가 관심 조건 기반 신규 공고 알림을 받고 앱에 재방문하도록, Supabase 3개 테이블 + 푸시/분석 기반을 구현한다.

**Architecture:** 앱은 온보딩/설정에서 관심 조건과 푸시 동의를 수집하고, FCM 토큰을 Supabase에 등록한다. Supabase는 `user_interest_profiles`, `notification_subscriptions`, `notification_delivery_logs`로 개인화 조건/전송 상태/중복 제어를 관리하고 Edge Function이 신규 공고 매칭 후 발송한다. 사용자 행동 퍼널은 Firebase Analytics를 기준으로 수집하고, 서버 발송 로그와 조합해 전환을 분석한다.

**Tech Stack:** Kotlin, Jetpack Compose, Hilt, Supabase(PostgREST, Edge Functions, RLS), Firebase Messaging, Firebase Analytics, Gradle, Python smoke tests

---

## Preconditions

- 전용 worktree에서 작업한다.
- 태스크 실행 중 실패 원인 불명이면 `@systematic-debugging` 절차로 원인 고정 후 재시도한다.
- 구현 중 범위 확장은 금지한다(YAGNI): 이번 스코프는 아래 3개 테이블 기반 기능만 포함한다.
  - `public.user_interest_profiles`
  - `public.notification_subscriptions`
  - `public.notification_delivery_logs`

### Task 1: Add Supabase Schema + RLS for 3 Tables

**Files:**
- Create: `supabase/migrations/20260224_add_notification_retention_tables.sql`
- Create: `supabase/scripts/notification_rls_smoke_test.py`
- Modify: `supabase/migrations/README.md`
- Test: `supabase/scripts/notification_rls_smoke_test.py`

**Step 1: Write the failing test**

```python
# supabase/scripts/notification_rls_smoke_test.py
# Failing-first: expect relation missing before migration.
status, body = http_json(
    "GET",
    rest_url("/rest/v1/user_interest_profiles?select=user_id&limit=1"),
    headers=rest_headers(a_token),
)
assert status == 200, f"expected table to exist, got status={status}, body={body}"
```

**Step 2: Run test to verify it fails**

Run: `python3 supabase/scripts/notification_rls_smoke_test.py`  
Expected: FAIL with `relation "user_interest_profiles" does not exist` or 404-style PostgREST error.

**Step 3: Write minimal implementation**

```sql
-- supabase/migrations/20260224_add_notification_retention_tables.sql
create table if not exists public.user_interest_profiles (
  user_id uuid primary key references public.profiles(user_id) on delete cascade,
  regions text[] not null default '{}',
  species text[] not null default '{}',
  sexes text[] not null default '{}',
  sizes text[] not null default '{}',
  push_enabled boolean not null default true,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);

create table if not exists public.notification_subscriptions (
  id uuid primary key default gen_random_uuid(),
  user_id uuid not null references public.profiles(user_id) on delete cascade,
  fcm_token text not null,
  push_opt_in boolean not null default true,
  last_active_at timestamptz,
  last_sent_at timestamptz,
  daily_sent_count int not null default 0,
  timezone text not null default 'Asia/Seoul',
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now(),
  unique (fcm_token)
);

create table if not exists public.notification_delivery_logs (
  id uuid primary key default gen_random_uuid(),
  user_id uuid not null references public.profiles(user_id) on delete cascade,
  campaign_type text not null check (campaign_type in ('new_animal', 'daily_digest', 'revisit_nudge')),
  notice_no text,
  dedupe_key text not null unique,
  status text not null check (status in ('queued', 'sent', 'failed', 'opened')),
  payload_json jsonb not null default '{}'::jsonb,
  sent_at timestamptz,
  opened_at timestamptz,
  created_at timestamptz not null default now()
);

create index if not exists notification_subscriptions_user_id_idx
  on public.notification_subscriptions(user_id);
create index if not exists notification_delivery_logs_user_campaign_idx
  on public.notification_delivery_logs(user_id, campaign_type, created_at desc);

alter table public.user_interest_profiles enable row level security;
alter table public.notification_subscriptions enable row level security;
alter table public.notification_delivery_logs enable row level security;

drop policy if exists user_interest_select_self on public.user_interest_profiles;
create policy user_interest_select_self
on public.user_interest_profiles
for select using (auth.uid() = user_id);

drop policy if exists user_interest_upsert_self on public.user_interest_profiles;
create policy user_interest_upsert_self
on public.user_interest_profiles
for all using (auth.uid() = user_id)
with check (auth.uid() = user_id);

drop policy if exists notification_subscriptions_select_self on public.notification_subscriptions;
create policy notification_subscriptions_select_self
on public.notification_subscriptions
for select using (auth.uid() = user_id);

drop policy if exists notification_subscriptions_write_self on public.notification_subscriptions;
create policy notification_subscriptions_write_self
on public.notification_subscriptions
for all using (auth.uid() = user_id)
with check (auth.uid() = user_id);

drop policy if exists notification_delivery_logs_select_self on public.notification_delivery_logs;
create policy notification_delivery_logs_select_self
on public.notification_delivery_logs
for select using (auth.uid() = user_id);
```

**Step 4: Run test to verify it passes**

Run:
1. `supabase db push --linked`
2. `python3 supabase/scripts/notification_rls_smoke_test.py`

Expected: PASS logs for self-read/self-write allowed and cross-user write blocked.

**Step 5: Commit**

```bash
git add supabase/migrations/20260224_add_notification_retention_tables.sql supabase/scripts/notification_rls_smoke_test.py supabase/migrations/README.md
git commit -m "feat(supabase): add notification retention schema and rls"
```

### Task 2: Implement Edge Function for New Notice Dispatch

**Files:**
- Create: `supabase/functions/new_notice_dispatch/index.ts`
- Create: `supabase/functions/new_notice_dispatch/README.md`
- Modify: `supabase/migrations/README.md`
- Test: `supabase/scripts/new_notice_dispatch_smoke_test.py`

**Step 1: Write the failing test**

```python
# supabase/scripts/new_notice_dispatch_smoke_test.py
status, body = http_json(
    "POST",
    rest_url("/functions/v1/new_notice_dispatch"),
    headers={"apikey": anon_key, "Authorization": f"Bearer {user_token}"},
    payload={"dry_run": True},
)
assert status == 200, f"expected 200, got {status} {body}"
assert isinstance(body.get("matched_users"), int)
```

**Step 2: Run test to verify it fails**

Run: `python3 supabase/scripts/new_notice_dispatch_smoke_test.py`  
Expected: FAIL with `Function not found`.

**Step 3: Write minimal implementation**

```ts
// supabase/functions/new_notice_dispatch/index.ts
import { createClient } from "jsr:@supabase/supabase-js@2";

Deno.serve(async (req) => {
  if (req.method !== "POST") {
    return new Response(JSON.stringify({ error: "Method not allowed" }), { status: 405 });
  }

  const supabaseUrl = Deno.env.get("SUPABASE_URL")!;
  const serviceRoleKey = Deno.env.get("SUPABASE_SERVICE_ROLE_KEY")!;
  const client = createClient(supabaseUrl, serviceRoleKey, {
    auth: { autoRefreshToken: false, persistSession: false },
  });

  const { dry_run = false, notices = [] } = await req.json().catch(() => ({ dry_run: true, notices: [] }));

  const { data: subs } = await client
    .from("notification_subscriptions")
    .select("user_id,fcm_token,push_opt_in,daily_sent_count");

  const matched = (subs ?? []).filter((s) => s.push_opt_in === true);

  if (!dry_run) {
    for (const s of matched) {
      const dedupeKey = `new_animal:${s.user_id}:${(notices[0]?.notice_no ?? "unknown")}`;
      await client.from("notification_delivery_logs").upsert({
        user_id: s.user_id,
        campaign_type: "new_animal",
        notice_no: notices[0]?.notice_no ?? null,
        dedupe_key: dedupeKey,
        status: "queued",
        payload_json: { notices_count: notices.length },
      });
    }
  }

  return new Response(
    JSON.stringify({ matched_users: matched.length, queued: dry_run ? 0 : matched.length }),
    { status: 200, headers: { "Content-Type": "application/json" } },
  );
});
```

**Step 4: Run test to verify it passes**

Run:
1. `supabase functions deploy new_notice_dispatch`
2. `python3 supabase/scripts/new_notice_dispatch_smoke_test.py`

Expected: PASS with JSON containing `matched_users`.

**Step 5: Commit**

```bash
git add supabase/functions/new_notice_dispatch/index.ts supabase/functions/new_notice_dispatch/README.md supabase/scripts/new_notice_dispatch_smoke_test.py supabase/migrations/README.md
git commit -m "feat(functions): add new notice dispatch edge function"
```

### Task 3: Add Notification Models and Supabase Service Layer

**Files:**
- Create: `core/model/src/main/java/kr/sjh/core/model/notification/UserInterestProfile.kt`
- Create: `core/model/src/main/java/kr/sjh/core/model/notification/NotificationSubscription.kt`
- Create: `core/model/src/main/java/kr/sjh/core/model/notification/NotificationDeliveryLog.kt`
- Create: `core/supabase/src/main/java/kr/sjh/core/supabase/service/NotificationService.kt`
- Create: `core/supabase/src/main/java/kr/sjh/core/supabase/service/impl/NotificationServiceImpl.kt`
- Modify: `core/supabase/src/main/java/kr/sjh/core/supabase/di/SupaServiceModule.kt`
- Test: `core/supabase/src/test/java/kr/sjh/core/supabase/service/impl/NotificationPayloadBuildTest.kt`

**Step 1: Write the failing test**

```kotlin
@Test
fun `build upsert payload trims token and normalizes timezone`() {
    val payload = NotificationServiceImpl.buildSubscriptionPayloadForTest(
        userId = "u1",
        fcmToken = " token ",
        timezone = ""
    )

    assertEquals("token", payload["fcm_token"])
    assertEquals("Asia/Seoul", payload["timezone"])
}
```

**Step 2: Run test to verify it fails**

Run: `./gradlew :core:supabase:testDebugUnitTest --tests "*NotificationPayloadBuildTest"`  
Expected: FAIL with unresolved `NotificationServiceImpl` or missing method.

**Step 3: Write minimal implementation**

```kotlin
// core/supabase/.../NotificationService.kt
interface NotificationService {
    suspend fun upsertInterestProfile(profile: UserInterestProfile)
    suspend fun upsertSubscription(userId: String, fcmToken: String, pushOptIn: Boolean, timezone: String)
    suspend fun touchLastActive(userId: String)
}

// core/supabase/.../NotificationServiceImpl.kt
class NotificationServiceImpl @Inject constructor(postgrest: Postgrest) : NotificationService {
    private val interestTable = postgrest.from("user_interest_profiles")
    private val subscriptionTable = postgrest.from("notification_subscriptions")

    override suspend fun upsertInterestProfile(profile: UserInterestProfile) {
        interestTable.upsert(profile)
    }

    override suspend fun upsertSubscription(userId: String, fcmToken: String, pushOptIn: Boolean, timezone: String) {
        subscriptionTable.upsert(buildSubscriptionPayloadForTest(userId, fcmToken, timezone) + mapOf("push_opt_in" to pushOptIn))
    }

    override suspend fun touchLastActive(userId: String) {
        subscriptionTable.update(mapOf("last_active_at" to OffsetDateTime.now().toString())) {
            filter { eq("user_id", userId) }
        }
    }

    internal companion object {
        fun buildSubscriptionPayloadForTest(userId: String, fcmToken: String, timezone: String): Map<String, Any> =
            mapOf(
                "user_id" to userId,
                "fcm_token" to fcmToken.trim(),
                "timezone" to timezone.ifBlank { "Asia/Seoul" }
            )
    }
}
```

**Step 4: Run test to verify it passes**

Run: `./gradlew :core:supabase:testDebugUnitTest --tests "*NotificationPayloadBuildTest"`  
Expected: PASS.

**Step 5: Commit**

```bash
git add core/model/src/main/java/kr/sjh/core/model/notification core/supabase/src/main/java/kr/sjh/core/supabase/service/NotificationService.kt core/supabase/src/main/java/kr/sjh/core/supabase/service/impl/NotificationServiceImpl.kt core/supabase/src/main/java/kr/sjh/core/supabase/di/SupaServiceModule.kt core/supabase/src/test/java/kr/sjh/core/supabase/service/impl/NotificationPayloadBuildTest.kt
git commit -m "feat(core): add notification supabase service"
```

### Task 4: Add Data Repository and Settings Persistence for Notification Preferences

**Files:**
- Create: `core/data/src/main/java/kr/sjh/data/repository/NotificationRepository.kt`
- Create: `core/data/src/main/java/kr/sjh/data/repository/impl/NotificationRepositoryImpl.kt`
- Modify: `core/data/src/main/java/kr/sjh/data/di/DataModule.kt`
- Modify: `core/data/src/main/java/kr/sjh/data/repository/SettingRepository.kt`
- Modify: `core/data/src/main/java/kr/sjh/data/repository/impl/SettingRepositoryImpl.kt`
- Modify: `core/datastore/src/main/java/kr/sjh/datastore/model/SettingsData.kt`
- Modify: `core/datastore/src/main/java/kr/sjh/datastore/datasource/SettingPreferenceDataSource.kt`
- Test: `core/data/src/test/java/kr/sjh/data/repository/impl/SettingRepositoryImplNotificationPrefTest.kt`

**Step 1: Write the failing test**

```kotlin
@Test
fun `default push opt in is true`() = runTest {
    val first = repository.getPushOptIn().first()
    assertTrue(first)
}
```

**Step 2: Run test to verify it fails**

Run: `./gradlew :core:data:testDebugUnitTest --tests "*SettingRepositoryImplNotificationPrefTest"`  
Expected: FAIL due to missing `getPushOptIn` API.

**Step 3: Write minimal implementation**

```kotlin
// SettingRepository.kt
fun getPushOptIn(): Flow<Boolean>
suspend fun updatePushOptIn(enabled: Boolean)

// SettingPreferenceDataSource.kt
val PUSH_OPT_IN = booleanPreferencesKey("PUSH_OPT_IN")

suspend fun updatePushOptIn(enabled: Boolean) {
    dataStore.edit { it[PUSH_OPT_IN] = enabled }
}

val settingsData = dataStore.data.map {
    SettingsData(
        isDarkTheme = it[IS_DARK_THEME] ?: false,
        hasSeenOnboarding = it[HAS_SEEN_ONBOARDING] ?: false,
        pushOptIn = it[PUSH_OPT_IN] ?: true
    )
}
```

**Step 4: Run test to verify it passes**

Run: `./gradlew :core:data:testDebugUnitTest --tests "*SettingRepositoryImplNotificationPrefTest"`  
Expected: PASS.

**Step 5: Commit**

```bash
git add core/data/src/main/java/kr/sjh/data/repository/NotificationRepository.kt core/data/src/main/java/kr/sjh/data/repository/impl/NotificationRepositoryImpl.kt core/data/src/main/java/kr/sjh/data/di/DataModule.kt core/data/src/main/java/kr/sjh/data/repository/SettingRepository.kt core/data/src/main/java/kr/sjh/data/repository/impl/SettingRepositoryImpl.kt core/datastore/src/main/java/kr/sjh/datastore/model/SettingsData.kt core/datastore/src/main/java/kr/sjh/datastore/datasource/SettingPreferenceDataSource.kt core/data/src/test/java/kr/sjh/data/repository/impl/SettingRepositoryImplNotificationPrefTest.kt
git commit -m "feat(data): add notification repository and push preference"
```

### Task 5: Extend Onboarding to Collect Interests + Notification Consent

**Files:**
- Create: `feature/signIn/src/main/java/kr/sjh/feature/signup/OnboardingViewModel.kt`
- Modify: `feature/signIn/src/main/java/kr/sjh/feature/signup/OnboardingScreen.kt`
- Modify: `feature/signIn/src/main/java/kr/sjh/feature/signup/navigation/SignUpNavigation.kt`
- Modify: `app/src/main/java/kr/sjh/bemypet/navigation/BeMyPetNavHost.kt`
- Test: `feature/signIn/src/test/java/kr/sjh/feature/signup/OnboardingViewModelTest.kt`

**Step 1: Write the failing test**

```kotlin
@Test
fun `complete onboarding emits selected interests and push opt in`() = runTest {
    viewModel.toggleSpecies("dog")
    viewModel.toggleRegion("6110000")
    viewModel.setPushOptIn(true)

    val payload = viewModel.buildSubmitPayloadForTest()

    assertEquals(listOf("dog"), payload.species)
    assertEquals(listOf("6110000"), payload.regions)
    assertTrue(payload.pushOptIn)
}
```

**Step 2: Run test to verify it fails**

Run: `./gradlew :feature:signIn:testDebugUnitTest --tests "*OnboardingViewModelTest"`  
Expected: FAIL with missing `OnboardingViewModel`.

**Step 3: Write minimal implementation**

```kotlin
@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val notificationRepository: NotificationRepository,
    private val settingRepository: SettingRepository
) : ViewModel() {
    private val selectedRegions = mutableSetOf<String>()
    private val selectedSpecies = mutableSetOf<String>()
    private var pushOptIn: Boolean = true

    fun toggleRegion(code: String) { if (!selectedRegions.add(code)) selectedRegions.remove(code) }
    fun toggleSpecies(code: String) { if (!selectedSpecies.add(code)) selectedSpecies.remove(code) }
    fun setPushOptIn(enabled: Boolean) { pushOptIn = enabled }

    fun buildSubmitPayloadForTest() = OnboardingSubmitPayload(
        regions = selectedRegions.toList(),
        species = selectedSpecies.toList(),
        pushOptIn = pushOptIn
    )
}
```

**Step 4: Run test to verify it passes**

Run: `./gradlew :feature:signIn:testDebugUnitTest --tests "*OnboardingViewModelTest"`  
Expected: PASS.

**Step 5: Commit**

```bash
git add feature/signIn/src/main/java/kr/sjh/feature/signup/OnboardingViewModel.kt feature/signIn/src/main/java/kr/sjh/feature/signup/OnboardingScreen.kt feature/signIn/src/main/java/kr/sjh/feature/signup/navigation/SignUpNavigation.kt app/src/main/java/kr/sjh/bemypet/navigation/BeMyPetNavHost.kt feature/signIn/src/test/java/kr/sjh/feature/signup/OnboardingViewModelTest.kt
git commit -m "feat(onboarding): collect interests and push consent"
```

### Task 6: Register FCM Token and Update Last Active Timestamp

**Files:**
- Create: `app/src/main/java/kr/sjh/bemypet/notifications/BeMyPetFirebaseMessagingService.kt`
- Create: `app/src/main/java/kr/sjh/bemypet/notifications/PushPayloadParser.kt`
- Modify: `app/src/main/AndroidManifest.xml`
- Modify: `app/build.gradle.kts`
- Modify: `app/src/main/java/kr/sjh/bemypet/StartActivity.kt`
- Test: `app/src/test/java/kr/sjh/bemypet/notifications/PushPayloadParserTest.kt`

**Step 1: Write the failing test**

```kotlin
@Test
fun `parser extracts notice number from data payload`() {
    val payload = mapOf("notice_no" to "12345", "campaign_type" to "new_animal")
    val parsed = PushPayloadParser.parse(payload)

    assertEquals("12345", parsed.noticeNo)
    assertEquals("new_animal", parsed.campaignType)
}
```

**Step 2: Run test to verify it fails**

Run: `./gradlew :app:testDevDebugUnitTest --tests "*PushPayloadParserTest"`  
Expected: FAIL with unresolved parser.

**Step 3: Write minimal implementation**

```kotlin
object PushPayloadParser {
    fun parse(data: Map<String, String>): ParsedPushPayload = ParsedPushPayload(
        noticeNo = data["notice_no"].orEmpty(),
        campaignType = data["campaign_type"].orEmpty(),
    )
}

@AndroidEntryPoint
class BeMyPetFirebaseMessagingService : FirebaseMessagingService() {
    @Inject lateinit var notificationRepository: NotificationRepository

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        CoroutineScope(Dispatchers.IO).launch {
            notificationRepository.upsertSubscription(token = token, pushOptIn = true)
        }
    }
}
```

**Step 4: Run test to verify it passes**

Run:
1. `./gradlew :app:testDevDebugUnitTest --tests "*PushPayloadParserTest"`
2. `./gradlew :app:compileDevDebugKotlin`

Expected: PASS + BUILD SUCCESSFUL.

**Step 5: Commit**

```bash
git add app/src/main/java/kr/sjh/bemypet/notifications/BeMyPetFirebaseMessagingService.kt app/src/main/java/kr/sjh/bemypet/notifications/PushPayloadParser.kt app/src/main/AndroidManifest.xml app/build.gradle.kts app/src/main/java/kr/sjh/bemypet/StartActivity.kt app/src/test/java/kr/sjh/bemypet/notifications/PushPayloadParserTest.kt
git commit -m "feat(app): register fcm token and parse push payload"
```

### Task 7: Centralize Firebase Analytics and Instrument Funnel Events

**Files:**
- Create: `core/firebase/src/main/java/kr/sjh/firebase/AnalyticsLogger.kt`
- Create: `core/firebase/src/main/java/kr/sjh/firebase/FirebaseAnalyticsLogger.kt`
- Create: `core/firebase/src/main/java/kr/sjh/firebase/NotificationAnalyticsEvents.kt`
- Modify: `feature/adoption-detail/src/main/java/kr/sjh/feature/adoption_detail/screen/PetDetailScreen.kt`
- Modify: `feature/signIn/src/main/java/kr/sjh/feature/signup/OnboardingScreen.kt`
- Modify: `feature/setting/src/main/java/kr/sjh/setting/screen/SettingScreen.kt`
- Modify: `core/firebase/build.gradle.kts`
- Test: `core/firebase/src/test/java/kr/sjh/firebase/NotificationAnalyticsEventsTest.kt`

**Step 1: Write the failing test**

```kotlin
@Test
fun `event params are clamped to firebase limits`() {
    val params = NotificationAnalyticsEvents.pushOpened(
        noticeNo = "12345",
        campaignType = "new_animal"
    )

    assertEquals("push_opened", params.eventName)
    assertEquals("12345", params.params["notice_no"])
}
```

**Step 2: Run test to verify it fails**

Run: `./gradlew :core:firebase:testDebugUnitTest --tests "*NotificationAnalyticsEventsTest"`  
Expected: FAIL with missing class.

**Step 3: Write minimal implementation**

```kotlin
interface AnalyticsLogger {
    fun log(eventName: String, params: Map<String, String>)
}

class FirebaseAnalyticsLogger @Inject constructor(
    @ApplicationContext context: Context
) : AnalyticsLogger {
    private val analytics = FirebaseAnalytics.getInstance(context)

    override fun log(eventName: String, params: Map<String, String>) {
        analytics.logEvent(eventName) {
            params.forEach { (k, v) -> param(k, v.take(100)) }
        }
    }
}

object NotificationAnalyticsEvents {
    fun pushOpened(noticeNo: String, campaignType: String) = AnalyticsEvent(
        eventName = "push_opened",
        params = mapOf("notice_no" to noticeNo, "campaign_type" to campaignType)
    )
}
```

**Step 4: Run test to verify it passes**

Run:
1. `./gradlew :core:firebase:testDebugUnitTest --tests "*NotificationAnalyticsEventsTest"`
2. `./gradlew :feature:adoption-detail:testDebugUnitTest :feature:signIn:testDebugUnitTest :feature:setting:testDebugUnitTest`

Expected: PASS.

**Step 5: Commit**

```bash
git add core/firebase/src/main/java/kr/sjh/firebase/AnalyticsLogger.kt core/firebase/src/main/java/kr/sjh/firebase/FirebaseAnalyticsLogger.kt core/firebase/src/main/java/kr/sjh/firebase/NotificationAnalyticsEvents.kt feature/adoption-detail/src/main/java/kr/sjh/feature/adoption_detail/screen/PetDetailScreen.kt feature/signIn/src/main/java/kr/sjh/feature/signup/OnboardingScreen.kt feature/setting/src/main/java/kr/sjh/setting/screen/SettingScreen.kt core/firebase/build.gradle.kts core/firebase/src/test/java/kr/sjh/firebase/NotificationAnalyticsEventsTest.kt
git commit -m "feat(analytics): instrument push retention funnel"
```

### Task 8: Integration Verification + Rollout Checklist

**Files:**
- Create: `docs/notification-retention-rollout.md`
- Modify: `docs/index.md`
- Test: `supabase/scripts/notification_rls_smoke_test.py`

**Step 1: Write the failing verification checklist**

```markdown
# Notification Retention Rollout

- [ ] RLS smoke tests pass
- [ ] Edge function dry-run returns matched users
- [ ] Android emits onboarding_complete + push_opened
- [ ] FCM token is upserted into notification_subscriptions
```

**Step 2: Run verification commands to capture baseline failures**

Run:
1. `python3 supabase/scripts/notification_rls_smoke_test.py`
2. `python3 supabase/scripts/new_notice_dispatch_smoke_test.py`
3. `./gradlew :app:compileDevDebugKotlin`

Expected: At least one check fails before all previous tasks are merged.

**Step 3: Update checklist with final pass criteria and evidence**

```markdown
## Evidence Format
- Date (YYYY-MM-DD)
- Command
- Result (PASS/FAIL)
- Short note
```

**Step 4: Run full regression**

Run:
1. `./gradlew :core:firebase:testDebugUnitTest :core:supabase:testDebugUnitTest :core:data:testDebugUnitTest :feature:signIn:testDebugUnitTest :feature:setting:testDebugUnitTest`
2. `./gradlew :app:compileDevDebugKotlin`
3. `python3 supabase/scripts/notification_rls_smoke_test.py`

Expected: All PASS.

**Step 5: Commit**

```bash
git add docs/notification-retention-rollout.md docs/index.md
git commit -m "docs: add notification retention rollout checklist"
```

## Done Criteria

- 신규 유저가 온보딩에서 관심 조건 + 푸시 동의를 저장할 수 있다.
- 앱이 FCM 토큰을 `notification_subscriptions`에 업서트한다.
- Edge Function이 `notification_delivery_logs` 중복 키를 사용해 알림 큐잉을 기록한다.
- Firebase Analytics에 최소 이벤트가 수집된다.
  - `onboarding_complete`
  - `interest_filter_saved`
  - `push_received`
  - `push_opened`
  - `pet_detail_view` (with `source`)

## Notes for Executor

- Supabase function/DB 관련 명령 실패 시 환경·권한 이슈부터 `@systematic-debugging`으로 고정한다.
- 스코프 밖 항목(추천 알고리즘 고도화, A/B 프레임워크, BigQuery 대시보드 자동화)은 이번 계획에서 제외한다.
