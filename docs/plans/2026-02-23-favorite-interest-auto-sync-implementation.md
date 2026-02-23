# Favorite-Based Interest Auto Sync Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** 관심목록에 등록된 펫 정보를 기반으로 사용자 관심사를 자동 추론하고, 서버 `user_interest_profiles`에 기존값과 합집합으로 동기화한다.

**Architecture:** 데이터 계층에 관심사 조회(`getInterestProfile`) API를 추가하고, 로컬 관심목록에서 관심사 추론을 담당하는 `FavoriteInterestProfileDeriver`와 동기화 오케스트레이션을 담당하는 `InterestProfileSyncCoordinator`를 도입한다. 동기화는 관심 추가/삭제 직후와 앱 시작 시 로그인 사용자 대상으로 1회 수행한다.

**Tech Stack:** Kotlin, Coroutines/Flow, Hilt DI, Room(FavouriteDao), Supabase PostgREST, JUnit

---

### Task 1: 서버 관심사 조회 경로 추가

**Files:**
- Modify: `core/supabase/src/main/java/kr/sjh/core/supabase/service/NotificationService.kt`
- Modify: `core/supabase/src/main/java/kr/sjh/core/supabase/service/impl/NotificationServiceImpl.kt`
- Modify: `core/data/src/main/java/kr/sjh/data/repository/NotificationRepository.kt`
- Modify: `core/data/src/main/java/kr/sjh/data/repository/impl/NotificationRepositoryImpl.kt`
- Test: `core/supabase/src/test/java/kr/sjh/core/supabase/service/impl/NotificationPayloadBuildTest.kt`

**Step 1: Write the failing test**

```kotlin
@Test
fun getInterestProfile_payload_is_normalized() {
    // user id trim + blank guard expectation
}
```

**Step 2: Run test to verify it fails**

Run: `./gradlew :core:supabase:testDebugUnitTest --tests "*NotificationPayloadBuildTest*" --no-daemon`
Expected: FAIL (new API absent)

**Step 3: Write minimal implementation**

```kotlin
interface NotificationService {
    suspend fun getInterestProfile(userId: String): UserInterestProfile?
}
```

```kotlin
override suspend fun getInterestProfile(userId: String): UserInterestProfile? {
    val normalized = userId.trim()
    if (normalized.isBlank()) return null
    return withAuthRefreshRetry("getInterestProfile") {
        interestTable.select {
            filter { eq("user_id", normalized) }
        }.decodeSingleOrNull<UserInterestProfile>()
    }
}
```

**Step 4: Run tests**

Run: `./gradlew :core:supabase:testDebugUnitTest --no-daemon`
Expected: PASS

**Step 5: Commit**

```bash
git add core/supabase/src/main/java/kr/sjh/core/supabase/service/NotificationService.kt \
  core/supabase/src/main/java/kr/sjh/core/supabase/service/impl/NotificationServiceImpl.kt \
  core/data/src/main/java/kr/sjh/data/repository/NotificationRepository.kt \
  core/data/src/main/java/kr/sjh/data/repository/impl/NotificationRepositoryImpl.kt \
  core/supabase/src/test/java/kr/sjh/core/supabase/service/impl/NotificationPayloadBuildTest.kt
git commit -m "feat(notification): add interest profile read API"
```

### Task 2: 관심목록 기반 관심사 추론기 구현

**Files:**
- Create: `core/data/src/main/java/kr/sjh/data/notification/FavoriteInterestProfileDeriver.kt`
- Create: `core/data/src/test/java/kr/sjh/data/notification/FavoriteInterestProfileDeriverTest.kt`

**Step 1: Write failing tests for derivation rules**

```kotlin
@Test fun derive_regions_from_notice_prefix()
@Test fun derive_species_from_kind_text_when_code_missing()
@Test fun derive_size_from_weight_threshold()
@Test fun dedupe_and_ignore_blank_values()
```

**Step 2: Run tests to verify fail**

Run: `./gradlew :core:data:testDebugUnitTest --tests "*FavoriteInterestProfileDeriverTest*" --no-daemon`
Expected: FAIL (class missing)

**Step 3: Implement minimal deriver**

```kotlin
data class DerivedInterestProfile(...)
object FavoriteInterestProfileDeriver {
    fun derive(pets: List<Pet>): DerivedInterestProfile { ... }
}
```

- 지역: noticeNo 접두 -> 시도코드 매핑
- 종: upKindCode 우선, 없으면 kind 문자열 휴리스틱
- 성별: M/F/Q만 사용
- 크기: SMALL/MEDIUM/LARGE

**Step 4: Run tests**

Run: `./gradlew :core:data:testDebugUnitTest --tests "*FavoriteInterestProfileDeriverTest*" --no-daemon`
Expected: PASS

**Step 5: Commit**

```bash
git add core/data/src/main/java/kr/sjh/data/notification/FavoriteInterestProfileDeriver.kt \
  core/data/src/test/java/kr/sjh/data/notification/FavoriteInterestProfileDeriverTest.kt
git commit -m "feat(notification): derive interests from favorites"
```

### Task 3: 관심사 병합/동기화 코디네이터 구현

**Files:**
- Create: `core/data/src/main/java/kr/sjh/data/notification/InterestProfileSyncCoordinator.kt`
- Create: `core/data/src/test/java/kr/sjh/data/notification/InterestProfileSyncCoordinatorTest.kt`
- Modify: `core/data/src/main/java/kr/sjh/data/di/DataModule.kt`

**Step 1: Write failing tests for merge policy**

```kotlin
@Test fun sync_merges_server_and_derived_by_union()
@Test fun sync_keeps_push_enabled_from_setting()
@Test fun sync_skips_when_user_blank()
```

**Step 2: Run tests to verify fail**

Run: `./gradlew :core:data:testDebugUnitTest --tests "*InterestProfileSyncCoordinatorTest*" --no-daemon`
Expected: FAIL

**Step 3: Implement coordinator**

```kotlin
suspend fun syncFromFavorites(userId: String) {
    val favorites = favouriteRepository.getFavouritePets().first()
    val derived = FavoriteInterestProfileDeriver.derive(favorites)
    val existing = notificationRepository.getInterestProfile(userId)
    val merged = union(existing, derived)
    notificationRepository.upsertInterestProfile(..., pushEnabled = settingPushOptIn)
}
```

**Step 4: Run tests**

Run: `./gradlew :core:data:testDebugUnitTest --tests "*InterestProfileSyncCoordinatorTest*" --no-daemon`
Expected: PASS

**Step 5: Commit**

```bash
git add core/data/src/main/java/kr/sjh/data/notification/InterestProfileSyncCoordinator.kt \
  core/data/src/test/java/kr/sjh/data/notification/InterestProfileSyncCoordinatorTest.kt \
  core/data/src/main/java/kr/sjh/data/di/DataModule.kt
git commit -m "feat(notification): add merged interest profile sync coordinator"
```

### Task 4: 관심목록 변경 시 즉시 동기화 연결

**Files:**
- Modify: `feature/adoption-detail/src/main/java/kr/sjh/feature/adoption_detail/screen/PetDetailViewModel.kt`
- Test: `feature/adoption-detail/src/test/java/kr/sjh/feature/adoption_detail/screen/PetDetailViewModelTest.kt` (create if absent)

**Step 1: Write failing tests**

```kotlin
@Test fun add_favorite_triggers_interest_sync_after_success()
@Test fun remove_favorite_triggers_interest_sync_after_success()
```

**Step 2: Run tests to verify fail**

Run: `./gradlew :feature:adoption-detail:testDebugUnitTest --no-daemon`
Expected: FAIL

**Step 3: Implement minimal integration**

```kotlin
if (favoriteToggleSucceeded) {
    interestProfileSyncCoordinator.syncFromCurrentSessionIfAuthenticated()
}
```

**Step 4: Run tests**

Run: `./gradlew :feature:adoption-detail:testDebugUnitTest --no-daemon`
Expected: PASS

**Step 5: Commit**

```bash
git add feature/adoption-detail/src/main/java/kr/sjh/feature/adoption_detail/screen/PetDetailViewModel.kt \
  feature/adoption-detail/src/test/java/kr/sjh/feature/adoption_detail/screen/PetDetailViewModelTest.kt
git commit -m "feat(favorite): sync interest profile on favorite toggle"
```

### Task 5: 앱 시작 시 1회 보정 동기화 추가

**Files:**
- Modify: `app/src/main/java/kr/sjh/bemypet/StartViewModel.kt`
- Modify: `app/src/main/java/kr/sjh/bemypet/StartActivity.kt` (필요 시)
- Test: `app/src/androidTest/java/kr/sjh/bemypet/notifications/PushRetentionFlowInstrumentedTest.kt`

**Step 1: Write failing test**

```kotlin
@Test fun authenticated_start_runs_interest_sync_once()
```

**Step 2: Run test to verify fail**

Run: `./gradlew :app:compileDevDebugAndroidTestKotlin --no-daemon`
Expected: FAIL

**Step 3: Implement minimal behavior**

```kotlin
fun syncInterestFromFavoritesIfNeeded(userId: String) { ... }
```

- 동일 사용자 재호출 중복 방지를 위해 in-memory guard 사용

**Step 4: Run tests**

Run: `./gradlew :app:compileDevDebugKotlin :app:compileDevDebugAndroidTestKotlin --no-daemon`
Expected: PASS

**Step 5: Commit**

```bash
git add app/src/main/java/kr/sjh/bemypet/StartViewModel.kt \
  app/src/main/java/kr/sjh/bemypet/StartActivity.kt \
  app/src/androidTest/java/kr/sjh/bemypet/notifications/PushRetentionFlowInstrumentedTest.kt
git commit -m "feat(app): sync favorite-derived interests on app start"
```

### Task 6: 통합 검증 및 문서 정리

**Files:**
- Modify: `docs/new-notice-dispatch-runbook.md`

**Step 1: Run regression command set**

Run:
- `./gradlew :core:data:testDebugUnitTest --no-daemon`
- `./gradlew :feature:adoption-detail:testDebugUnitTest --no-daemon`
- `./gradlew :app:compileDevDebugKotlin --no-daemon`

Expected: PASS

**Step 2: Manual verification checklist**
- 로그인 후 관심목록 추가
- `user_interest_profiles`에 regions/species/sexes/sizes 반영 확인
- `new_notice_dispatch` 수동 실행 후 `matched_users` 증감 확인

**Step 3: Commit docs**

```bash
git add docs/new-notice-dispatch-runbook.md docs/plans/2026-02-23-favorite-interest-auto-sync-design.md docs/plans/2026-02-23-favorite-interest-auto-sync-implementation.md
git commit -m "docs(notification): add favorite-based interest sync rollout notes"
```
