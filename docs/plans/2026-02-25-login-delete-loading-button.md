# 로그인/회원탈퇴 버튼 로딩 피드백 Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** 로그인(구글/카카오)과 회원탈퇴 액션에서 버튼 자체 로딩 상태를 표시하고 중복 탭을 방지한다.

**Architecture:** ViewModel UI 상태를 단일 소스로 확장해(로그인 provider별 로딩, 회원탈퇴 로딩) Compose 버튼 렌더링을 제어한다. 다이얼로그 확인 클릭 시 즉시 닫고, 계정 카드의 회원탈퇴 버튼에서 진행 상태를 보여준다. 기존 성공/실패/세션만료 분기와 스낵바 메시지는 유지한다.

**Tech Stack:** Kotlin, AndroidX Compose, StateFlow, Hilt ViewModel, JUnit4 + kotlinx-coroutines-test

---

### Task 1: 로그인 로딩 상태 모델 분리

**Files:**
- Modify: `feature/signIn/src/main/java/kr/sjh/feature/signup/SignInViewModel.kt`

**Step 1: Write the failing test**
- Plan test case: Google 로그인 시작 시 `loadingProvider=Google`, 완료 후 `null`.
- Plan test case: Kakao 로그인 시작 시 `loadingProvider=Kakao`, 완료 후 `null`.

**Step 2: Run test to verify it fails**
Run: `./gradlew :feature:signIn:testDebugUnitTest --tests "*SignInViewModel*" --no-daemon`
Expected: 테스트 파일/타입 미존재로 실패 또는 컴파일 실패.

**Step 3: Write minimal implementation**
- `SignInUiState`에 provider 기반 상태 추가:
  - `enum class LoadingProvider { Google, Kakao }`
  - `val loadingProvider: LoadingProvider? = null`
  - `val isLoading: Boolean get() = loadingProvider != null`
- Google/Kakao 시작 지점에서 provider 세팅.
- onSuccess/onFailure에서 provider 해제.

**Step 4: Run test to verify it passes**
Run: `./gradlew :feature:signIn:testDebugUnitTest --tests "*SignInViewModel*" --no-daemon`
Expected: PASS.

**Step 5: Commit**
```bash
git add feature/signIn/src/main/java/kr/sjh/feature/signup/SignInViewModel.kt
git commit -m "feat(signin): provider별 버튼 로딩 상태 도입"
```

### Task 2: 로그인 버튼 UI를 버튼 내부 로딩으로 전환

**Files:**
- Modify: `feature/signIn/src/main/java/kr/sjh/feature/signup/SignInScreen.kt`

**Step 1: Write the failing test**
- Compose UI 테스트 대신 수동 시나리오 기준 정의:
  - Google 진행 중 Google 버튼이 `처리 중...` + spinner 표시.
  - Kakao 진행 중 Kakao 버튼이 `처리 중...` + spinner 표시.
  - 두 버튼은 로딩 중 비활성.

**Step 2: Run test to verify it fails**
Run app manually and verify current behavior (버튼 내부 로딩 없음).
Expected: 요구사항과 불일치.

**Step 3: Write minimal implementation**
- Social 버튼 컴포저블에서 `loadingProvider` 기반 분기 렌더링.
- 버튼 공통 로딩 content(`spinner + 처리 중...`) 추가.
- 기존 하단 보조 로딩 행("로그인 진행 중...") 제거.

**Step 4: Run test to verify it passes**
Run: `./gradlew :app:compileDevDebugKotlin --no-daemon`
Expected: PASS.
Manual: 버튼 로딩 동작 확인.

**Step 5: Commit**
```bash
git add feature/signIn/src/main/java/kr/sjh/feature/signup/SignInScreen.kt
git commit -m "feat(signin-ui): 소셜 로그인 버튼 내부 로딩 표시 적용"
```

### Task 3: 회원탈퇴 로딩 상태 모델 추가

**Files:**
- Modify: `feature/setting/src/main/java/kr/sjh/setting/screen/SettingViewModel.kt`
- Modify/Test: `feature/setting/src/test/java/kr/sjh/setting/screen/SettingViewModelProfileUploadTest.kt`

**Step 1: Write the failing test**
- `deleteAccount` 호출 직후 `isDeletingAccount=true`, 완료 후 `false` 검증 테스트 추가.

**Step 2: Run test to verify it fails**
Run: `./gradlew :feature:setting:testDebugUnitTest --tests "*SettingViewModelProfileUploadTest*" --no-daemon`
Expected: 신규 상태 필드/동작 부재로 실패.

**Step 3: Write minimal implementation**
- `ProfileUiState`에 `isDeletingAccount: Boolean = false` 추가.
- `deleteAccount`에서 시작 시 true, success/failure 콜백에서 false 해제.

**Step 4: Run test to verify it passes**
Run: `./gradlew :feature:setting:testDebugUnitTest --tests "*SettingViewModelProfileUploadTest*" --no-daemon`
Expected: PASS.

**Step 5: Commit**
```bash
git add feature/setting/src/main/java/kr/sjh/setting/screen/SettingViewModel.kt \
        feature/setting/src/test/java/kr/sjh/setting/screen/SettingViewModelProfileUploadTest.kt
git commit -m "feat(setting): 회원탈퇴 진행 상태 플래그 추가"
```

### Task 4: 설정 화면 회원탈퇴 버튼 로딩 반영

**Files:**
- Modify: `feature/setting/src/main/java/kr/sjh/setting/screen/SettingScreen.kt`

**Step 1: Write the failing test**
- 수동 기준:
  - 다이얼로그 확인 클릭 시 즉시 닫힘.
  - 카드 내 회원탈퇴 버튼이 `처리 중...` + spinner.
  - 탈퇴 진행 중 프로필수정/로그아웃/회원탈퇴 클릭 불가.

**Step 2: Run test to verify it fails**
Run app manually and 확인.
Expected: 현재는 버튼 로딩 및 비활성 제어 없음.

**Step 3: Write minimal implementation**
- `SettingRoute`에서 다이얼로그 confirm 시 `hideDeleteUserDialog()` 먼저 호출.
- `ProfileAccountSection` 파라미터에 `isDeletingAccount` 추가.
- 버튼 렌더링/enable 상태를 `isDeletingAccount`로 제어.
- 삭제 버튼 로딩 콘텐츠(스피너 + 처리 중...) 추가.

**Step 4: Run test to verify it passes**
Run: `./gradlew :app:compileDevDebugKotlin --no-daemon`
Expected: PASS.
Manual: 회원탈퇴 UX 확인.

**Step 5: Commit**
```bash
git add feature/setting/src/main/java/kr/sjh/setting/screen/SettingScreen.kt
git commit -m "feat(setting-ui): 회원탈퇴 버튼 로딩 상태 및 중복 클릭 방지"
```

### Task 5: 최종 검증 및 문서 동기화

**Files:**
- Modify: `docs/plans/2026-02-25-login-delete-loading-button-design.md` (필요 시 검증 결과 반영)

**Step 1: Run full verification**
Run:
- `./gradlew :feature:signIn:testDebugUnitTest --no-daemon`
- `./gradlew :feature:setting:testDebugUnitTest --no-daemon`
- `./gradlew :app:compileDevDebugKotlin --no-daemon`

Expected: PASS.

**Step 2: Manual sanity check**
- 로그인 버튼 동작 2종(Google/Kakao)
- 회원탈퇴 다이얼로그 닫힘 + 버튼 로딩 + 완료 후 복구

**Step 3: Commit**
```bash
git add docs/plans/2026-02-25-login-delete-loading-button-design.md \
        docs/plans/2026-02-25-login-delete-loading-button.md
git commit -m "docs: 로그인/회원탈퇴 버튼 로딩 설계 및 구현 계획 문서화"
```
