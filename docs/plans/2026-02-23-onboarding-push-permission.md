# Onboarding Push Permission Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** 온보딩 푸시 동의가 실제 권한 요청/저장/서버 동기화로 동작하고, 설정 화면에서도 OFF->ON 시 권한 재요청이 가능하도록 만든다.

**Architecture:** 권한 요청은 온보딩/설정 UI에서 직접 수행하고, ViewModel은 최종 동의 상태를 저장한다. FCM 토큰 기반 Supabase 구독 동기화는 앱 시작 컨트롤러(StartActivity/StartViewModel)에서 `session + onboarding 완료 + pushOptIn` 상태를 기준으로 일관되게 수행한다.

**Tech Stack:** Kotlin, Jetpack Compose, Activity Result API, Android 13 Notification Permission, DataStore, Firebase Messaging, Supabase(PostgREST), Hilt, Coroutines Test

---

### Task 1: Add Resolved Push Opt-In Path in OnboardingViewModel

**Files:**
- Modify: `feature/signIn/src/main/java/kr/sjh/feature/signup/OnboardingViewModel.kt`
- Modify: `feature/signIn/src/test/java/kr/sjh/feature/signup/OnboardingViewModelTest.kt`

**Step 1: Write the failing test**

```kotlin
@Test
fun `submit with permission denied stores push opt out`() = runTest {
    val settingRepository = FakeSettingRepository()
    val viewModel = OnboardingViewModel(
        notificationRepository = FakeNotificationRepository(),
        settingRepository = settingRepository,
    )
    viewModel.setPushOptIn(true)

    viewModel.submit(
        session = SessionState.NoAuthenticated,
        resolvedPushOptIn = false,
    )

    assertFalse(settingRepository.lastPushOptIn)
}
```

**Step 2: Run test to verify it fails**

Run: `./gradlew :feature:signIn:testDebugUnitTest --tests "*OnboardingViewModelTest"`  
Expected: FAIL with unresolved `resolvedPushOptIn` parameter or assertion mismatch.

**Step 3: Write minimal implementation**

```kotlin
fun submit(session: SessionState, resolvedPushOptIn: Boolean? = null) {
    val payload = buildSubmitPayloadForTest().let {
        if (resolvedPushOptIn == null) it else it.copy(pushOptIn = resolvedPushOptIn)
    }
    // existing save/upsert logic
}
```

**Step 4: Run test to verify it passes**

Run: `./gradlew :feature:signIn:testDebugUnitTest --tests "*OnboardingViewModelTest"`  
Expected: PASS

**Step 5: Commit**

```bash
git add feature/signIn/src/main/java/kr/sjh/feature/signup/OnboardingViewModel.kt feature/signIn/src/test/java/kr/sjh/feature/signup/OnboardingViewModelTest.kt
git commit -m "feat(onboarding): support resolved push opt-in on submit"
```

### Task 2: Request Notification Permission on Onboarding Final Action

**Files:**
- Modify: `feature/signIn/src/main/java/kr/sjh/feature/signup/OnboardingScreen.kt`

**Step 1: Write the failing test (logic-focused helper)**

```kotlin
@Test
fun `resolve push opt in returns false when desired true but permission denied`() {
    val resolved = resolvePushOptIn(desired = true, hasPermission = false, permissionGranted = false)
    assertFalse(resolved)
}
```

**Step 2: Run test to verify it fails**

Run: `./gradlew :feature:signIn:testDebugUnitTest --tests "*OnboardingPermission*"`  
Expected: FAIL due to missing helper/function.

**Step 3: Write minimal implementation**

```kotlin
val permissionLauncher = rememberLauncherForActivityResult(
    ActivityResultContracts.RequestPermission()
) { granted ->
    val resolved = if (desiredPushOptIn && !granted) false else desiredPushOptIn
    viewModel.submit(session, resolvedPushOptIn = resolved)
    onComplete()
}
```

핵심 구현 규칙:
- 마지막 페이지 + `pushOptIn=true` + Android 13+ + 미허용일 때만 요청
- 거부 시 `resolvedPushOptIn=false`로 submit
- `pushOptIn=false`이면 요청 없이 submit
- `건너뛰기`는 권한 요청 없이 submit(`resolvedPushOptIn=false`) 후 종료

**Step 4: Run compile/test verification**

Run: `./gradlew :feature:signIn:compileDebugKotlin :feature:signIn:testDebugUnitTest --no-daemon`  
Expected: PASS

**Step 5: Commit**

```bash
git add feature/signIn/src/main/java/kr/sjh/feature/signup/OnboardingScreen.kt
git commit -m "feat(onboarding): request notification permission on final opt-in"
```

### Task 3: Add Push Preference State and Update API in SettingViewModel

**Files:**
- Modify: `feature/setting/src/main/java/kr/sjh/setting/screen/SettingViewModel.kt`
- Modify: `feature/setting/src/test/java/kr/sjh/setting/screen/SettingViewModelProfileUploadTest.kt`
- Create: `feature/setting/src/test/java/kr/sjh/setting/screen/SettingViewModelPushOptInTest.kt`

**Step 1: Write the failing test**

```kotlin
@Test
fun `setPushOptIn updates state and repository`() = runTest {
    val fakeSettingRepository = FakeSettingRepository(initialPushOptIn = true)
    val viewModel = createSettingViewModel(settingRepository = fakeSettingRepository)

    viewModel.setPushOptIn(false)

    assertFalse(viewModel.profileUiState.value.pushOptIn)
    assertFalse(fakeSettingRepository.lastUpdatedPushOptIn)
}
```

**Step 2: Run test to verify it fails**

Run: `./gradlew :feature:setting:testDebugUnitTest --tests "*SettingViewModelPushOptInTest"`  
Expected: FAIL because `pushOptIn` state/API not implemented.

**Step 3: Write minimal implementation**

```kotlin
data class ProfileUiState(
    ...
    val pushOptIn: Boolean = true,
)

fun observePushOptIn() = viewModelScope.launch {
    settingRepository.getPushOptIn().collect { enabled ->
        _profileUiState.update { it.copy(pushOptIn = enabled) }
    }
}

fun setPushOptIn(enabled: Boolean) = viewModelScope.launch {
    settingRepository.updatePushOptIn(enabled)
}
```

**Step 4: Run test to verify it passes**

Run: `./gradlew :feature:setting:testDebugUnitTest --tests "*SettingViewModelPushOptInTest"`  
Expected: PASS

**Step 5: Commit**

```bash
git add feature/setting/src/main/java/kr/sjh/setting/screen/SettingViewModel.kt feature/setting/src/test/java/kr/sjh/setting/screen/SettingViewModelPushOptInTest.kt feature/setting/src/test/java/kr/sjh/setting/screen/SettingViewModelProfileUploadTest.kt
git commit -m "feat(setting): add push opt-in state management in viewmodel"
```

### Task 4: Add Push Toggle in SettingScreen with OFF->ON Permission Re-Request

**Files:**
- Modify: `feature/setting/src/main/java/kr/sjh/setting/screen/SettingScreen.kt`
- Modify: `feature/setting/build.gradle.kts` (if `firebase-messaging` reference is required in UI layer)

**Step 1: Write the failing test (UI logic helper)**

```kotlin
@Test
fun `toggle on without permission keeps disabled when denied`() {
    val result = resolveSettingPushToggle(targetEnabled = true, hasPermission = false, permissionGranted = false)
    assertFalse(result)
}
```

**Step 2: Run test to verify it fails**

Run: `./gradlew :feature:setting:testDebugUnitTest --tests "*SettingPushToggle*"`  
Expected: FAIL with missing helper or behavior.

**Step 3: Write minimal implementation**

```kotlin
Switch(
    checked = uiState.pushOptIn,
    onCheckedChange = { target ->
        if (!target) {
            viewModel.setPushOptIn(false)
        } else if (hasPermission()) {
            viewModel.setPushOptIn(true)
        } else {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
)
```

권한 결과 콜백:
- granted=true -> `viewModel.setPushOptIn(true)`
- granted=false -> `viewModel.setPushOptIn(false)` + 안내 메시지 이벤트

**Step 4: Run compile/test verification**

Run: `./gradlew :feature:setting:compileDebugKotlin :feature:setting:testDebugUnitTest --no-daemon`  
Expected: PASS

**Step 5: Commit**

```bash
git add feature/setting/src/main/java/kr/sjh/setting/screen/SettingScreen.kt feature/setting/build.gradle.kts
git commit -m "feat(setting): add push permission-aware toggle"
```

### Task 5: Move Push Subscription Sync Trigger to State-Based Orchestration and Remove Global Prompt

**Files:**
- Modify: `app/src/main/java/kr/sjh/bemypet/StartViewModel.kt`
- Modify: `app/src/main/java/kr/sjh/bemypet/StartActivity.kt`
- Optional Test: `app/src/test/java/kr/sjh/bemypet/StartViewModelPushSyncTest.kt`

**Step 1: Write the failing test**

```kotlin
@Test
fun `sync runs only when authenticated and onboarding seen`() = runTest {
    // arrange fake flows
    // assert sync not called until hasSeenOnboarding=true
}
```

**Step 2: Run test to verify it fails**

Run: `./gradlew :app:testDevDebugUnitTest --tests "*StartViewModelPushSyncTest"`  
Expected: FAIL due missing gating behavior/test scaffolding.

**Step 3: Write minimal implementation**

```kotlin
// StartViewModel
val pushOptIn = settingRepository.getPushOptIn()

// StartActivity
// remove ensureNotificationPermissionIfNeeded() from onStart
// collect session + hasSeenOnboarding + pushOptIn
// when authenticated && hasSeenOnboarding:
//   FirebaseMessaging.getInstance().token -> syncPushSubscription(userId, token)
```

검증 포인트:
- 온보딩 전에는 서버 구독 동기화되지 않음
- 온보딩 완료/설정 토글 변경 시 동기화 수행
- 앱 시작 시 강제 권한 팝업 제거

**Step 4: Run app-level verification**

Run: `./gradlew :app:compileDevDebugKotlin :app:testDevDebugUnitTest --no-daemon`  
Expected: PASS

**Step 5: Commit**

```bash
git add app/src/main/java/kr/sjh/bemypet/StartViewModel.kt app/src/main/java/kr/sjh/bemypet/StartActivity.kt app/src/test/java/kr/sjh/bemypet/StartViewModelPushSyncTest.kt
git commit -m "feat(app): gate push sync by onboarding and remove global prompt"
```

### Task 6: End-to-End Verification and Docs Update

**Files:**
- Modify: `docs/index.md`
- Create (optional): `docs/push-onboarding-setting-qa.md`

**Step 1: Write manual QA checklist**

```md
- [ ] 온보딩 ON + 허용 => pushOptIn=true 저장 및 서버 반영
- [ ] 온보딩 ON + 거부 => pushOptIn=false 저장
- [ ] 설정 OFF->ON + 거부 => OFF 유지
- [ ] 설정 OFF->ON + 허용 => ON 반영
```

**Step 2: Run final verification commands**

Run:
1. `./gradlew :feature:signIn:testDebugUnitTest --no-daemon`
2. `./gradlew :feature:setting:testDebugUnitTest --no-daemon`
3. `./gradlew :app:compileDevDebugKotlin --no-daemon`

Expected: PASS

**Step 3: Commit docs/checklist**

```bash
git add docs/index.md docs/push-onboarding-setting-qa.md
git commit -m "docs: add onboarding/setting push permission QA checklist"
```

