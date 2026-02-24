# Kakao Login (dev/prod 분리) Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Supabase OAuth 기반 Kakao 로그인을 추가하고 dev/prod 환경별로 분리된 딥링크 및 설정으로 안정적으로 동작하게 만든다.

**Architecture:** 인증 진입은 `feature/signIn`에서 수행하고, 실제 로그인/검증 로직은 `core/data -> core/supabase` 체인으로 위임한다. OAuth 콜백은 `StartActivity`에서 `handleDeeplinks`로 처리하고, Supabase Auth config와 Android Manifest를 환경별 URI로 정렬한다.

**Tech Stack:** Kotlin, Jetpack Compose, Hilt, Supabase Kotlin SDK(Auth/PKCE), Android Manifest placeholders

---

### Task 1: OAuth Deeplink 환경 분리 설정

**Files:**
- Modify: `app/build.gradle.kts`
- Modify: `app/src/main/AndroidManifest.xml`
- Modify: `core/supabase/build.gradle.kts`

**Step 1: 앱 manifest placeholder 추가**

- `debug`/`release` 각각 `AUTH_SCHEME`, `AUTH_HOST` 값을 설정한다.
- 값:
  - debug: `bemypet-dev`, `oauth`
  - release: `bemypet`, `oauth`

**Step 2: Manifest OAuth data를 placeholder로 치환**

- `<data android:scheme="${AUTH_SCHEME}" android:host="${AUTH_HOST}" />` 형태로 변경한다.

**Step 3: supabase module BuildConfig 추가**

- `SUPABASE_AUTH_SCHEME`, `SUPABASE_AUTH_HOST`를 buildType별 buildConfigField로 추가한다.

**Step 4: 컴파일 확인**

Run: `./gradlew :app:compileDevDebugKotlin --no-daemon`  
Expected: BUILD SUCCESSFUL

**Step 5: Commit**

```bash
git add app/build.gradle.kts app/src/main/AndroidManifest.xml core/supabase/build.gradle.kts
git commit -m "feat: split oauth deeplink config for dev and prod"
```

### Task 2: Supabase Deeplink 처리 연결

**Files:**
- Modify: `core/supabase/src/main/java/kr/sjh/core/supabase/di/SupabaseModule.kt`
- Modify: `app/src/main/java/kr/sjh/bemypet/StartActivity.kt`

**Step 1: Auth config에 scheme/host 주입**

- `install(Auth)` 블록에 `scheme`, `host`, `flowType = FlowType.PKCE`를 모두 설정한다.

**Step 2: StartActivity 딥링크 처리 추가**

- `SupabaseClient`를 Hilt 주입한다.
- `onCreate`와 `onNewIntent`에서 `supabaseClient.handleDeeplinks(intent)` 호출을 추가한다.

**Step 3: 컴파일 확인**

Run: `./gradlew :app:compileDevDebugKotlin --no-daemon`  
Expected: BUILD SUCCESSFUL

**Step 4: Commit**

```bash
git add core/supabase/src/main/java/kr/sjh/core/supabase/di/SupabaseModule.kt app/src/main/java/kr/sjh/bemypet/StartActivity.kt
git commit -m "feat: wire supabase oauth deeplink handling"
```

### Task 3: Kakao 로그인 도메인 메서드 추가

**Files:**
- Modify: `core/supabase/src/main/java/kr/sjh/core/supabase/service/AuthService.kt`
- Modify: `core/supabase/src/main/java/kr/sjh/core/supabase/service/impl/AuthServiceImpl.kt`
- Modify: `core/data/src/main/java/kr/sjh/data/repository/AuthRepository.kt`
- Modify: `core/data/src/main/java/kr/sjh/data/repository/impl/AuthRepositoryImpl.kt`

**Step 1: 인터페이스에 signInWithKakao 추가**

- `onSuccess`, `onFailure` 콜백 구조를 Google과 동일하게 맞춘다.

**Step 2: AuthServiceImpl 구현**

- `auth.signInWith(Kakao)`로 OAuth 시작
- 로그인 완료 후 사용자 정보 검사:
  - `email` 비어있음 -> 실패
  - `emailConfirmedAt == null` -> 실패
- 실패 케이스는 `auth.signOut(SignOutScope.LOCAL)` 후 에러 반환

**Step 3: 저장소 계층 위임 연결**

- Repository impl에서 service 메서드 위임만 수행한다.

**Step 4: 컴파일 확인**

Run: `./gradlew :core:data:compileDebugKotlin :core:supabase:compileDebugKotlin --no-daemon`  
Expected: BUILD SUCCESSFUL

**Step 5: Commit**

```bash
git add core/supabase/src/main/java/kr/sjh/core/supabase/service/AuthService.kt core/supabase/src/main/java/kr/sjh/core/supabase/service/impl/AuthServiceImpl.kt core/data/src/main/java/kr/sjh/data/repository/AuthRepository.kt core/data/src/main/java/kr/sjh/data/repository/impl/AuthRepositoryImpl.kt
git commit -m "feat: add kakao sign-in in auth and repository layers"
```

### Task 4: SignIn 화면/상태 로직 연동

**Files:**
- Modify: `feature/signIn/src/main/java/kr/sjh/feature/signup/SignInScreen.kt`
- Modify: `feature/signIn/src/main/java/kr/sjh/feature/signup/SignInViewModel.kt`

**Step 1: Kakao 버튼 placeholder 제거**

- `"카카오 로그인은 준비 중이에요."` 대신 실제 ViewModel 호출로 연결한다.

**Step 2: ViewModel에 Kakao 분기 추가**

- `onKakaoSignIn()` 추가
- 로딩/성공/실패 상태를 Google과 동일 패턴으로 적용
- provider별 실패 메시지 매핑(`google`, `kakao`) 도입

**Step 3: 컴파일 확인**

Run: `./gradlew :feature:signIn:compileDebugKotlin --no-daemon`  
Expected: BUILD SUCCESSFUL

**Step 4: Commit**

```bash
git add feature/signIn/src/main/java/kr/sjh/feature/signup/SignInScreen.kt feature/signIn/src/main/java/kr/sjh/feature/signup/SignInViewModel.kt
git commit -m "feat: connect kakao sign-in button and state handling"
```

### Task 5: 테스트 더블/문서 정리 및 최종 검증

**Files:**
- Modify: `feature/setting/src/test/java/kr/sjh/setting/screen/SettingViewModelProfileUploadTest.kt`
- Modify: `app/src/androidTest/java/kr/sjh/bemypet/notifications/PushRetentionFlowInstrumentedTest.kt`
- Modify: `core/data/src/test/java/kr/sjh/data/repository/impl/AuthRepositoryImplUploadAvatarTest.kt`
- Modify: `README.md`

**Step 1: AuthRepository/AuthService 인터페이스 변경 대응**

- 테스트 Fake 구현체에 `signInWithKakao` 스텁을 추가해 컴파일을 맞춘다.

**Step 2: README에 Kakao 로그인/환경 설정 가이드 추가**

- dev/prod OAuth redirect URI 분리와 Supabase provider 설정 포인트를 문서화한다.

**Step 3: 최종 검증**

Run:
- `./gradlew :app:compileDevDebugKotlin --no-daemon`
- `./gradlew :app:compileProdReleaseKotlin --no-daemon`
- `./gradlew :feature:setting:testDebugUnitTest :core:data:testDebugUnitTest --no-daemon`

Expected: 모든 명령 BUILD SUCCESSFUL / tests passed

**Step 4: Commit**

```bash
git add feature/setting/src/test/java/kr/sjh/setting/screen/SettingViewModelProfileUploadTest.kt app/src/androidTest/java/kr/sjh/bemypet/notifications/PushRetentionFlowInstrumentedTest.kt core/data/src/test/java/kr/sjh/data/repository/impl/AuthRepositoryImplUploadAvatarTest.kt README.md
git commit -m "chore: update tests and docs for kakao login rollout"
```
