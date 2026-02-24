# Kakao 이메일 확인 가이드 화면 Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Kakao 로그인 후 이메일 없음/미인증 사용자를 전용 안내 화면으로 분기하고 앱 설정 이동/재로그인 액션을 제공한다.

**Architecture:** 인증 상태 판정은 `core/supabase`의 session flow에서 수행하고, 결과를 `SessionState.EmailVerificationRequired`로 모델링한다. `app` 네비게이션은 해당 상태를 감지해 안내 화면으로 라우팅하며, 화면 액션은 설정 이동 및 로컬 로그아웃을 수행한다.

**Tech Stack:** Kotlin, Jetpack Compose, Navigation Compose, Supabase Auth session flow

---

### Task 1: 세션 상태 모델 확장

**Files:**
- Modify: `core/model/src/main/java/kr/sjh/core/model/SessionState.kt`

**Step 1: 이메일 확인 요구 reason 타입 추가**
- `KakaoEmailVerificationReason` enum 추가 (`NO_EMAIL`, `UNVERIFIED_EMAIL`)

**Step 2: SessionState에 신규 상태 추가**
- `EmailVerificationRequired(reason: KakaoEmailVerificationReason)` 추가

**Step 3: 컴파일 확인**
Run: `./gradlew :core:model:compileDebugKotlin --no-daemon`  
Expected: BUILD SUCCESSFUL

### Task 2: AuthService session flow에서 Kakao 이메일 판정 추가

**Files:**
- Modify: `core/supabase/src/main/java/kr/sjh/core/supabase/service/impl/AuthServiceImpl.kt`

**Step 1: provider 식별 로직 추가**
- `appMetadata.provider` 또는 `identities.provider`에서 `kakao` 여부 판정

**Step 2: 이메일 조건 검사**
- Kakao 사용자에서
  - 이메일 비어있으면 `NO_EMAIL`
  - `emailConfirmedAt == null`이면 `UNVERIFIED_EMAIL`

**Step 3: session flow 분기 추가**
- `SessionStatus.Authenticated`에서 조건 불충족이면 `SessionState.EmailVerificationRequired(reason)` 반환

**Step 4: 컴파일 확인**
Run: `./gradlew :core:supabase:compileDebugKotlin --no-daemon`  
Expected: BUILD SUCCESSFUL

### Task 3: 안내 화면 라우트/화면 추가

**Files:**
- Modify: `feature/signIn/src/main/java/kr/sjh/feature/signup/navigation/SignUpNavigation.kt`
- Add: `feature/signIn/src/main/java/kr/sjh/feature/signup/KakaoEmailVerificationScreen.kt`

**Step 1: route 추가**
- `KakaoEmailVerificationNotice(reason: String)` route 타입 추가

**Step 2: 화면 구현**
- 케이스별 문구 렌더링
- `앱 설정 열기` 버튼
- `다시 로그인` 버튼 콜백 제공

**Step 3: 컴파일 확인**
Run: `./gradlew :feature:signIn:compileDebugKotlin --no-daemon`  
Expected: BUILD SUCCESSFUL

### Task 4: SignInRoute와 NavHost 연동

**Files:**
- Modify: `feature/signIn/src/main/java/kr/sjh/feature/signup/SignInScreen.kt`
- Modify: `app/src/main/java/kr/sjh/bemypet/navigation/BeMyPetNavHost.kt`

**Step 1: SignInRoute가 session 상태 관찰**
- `session` 파라미터 추가
- `EmailVerificationRequired`이면 안내 화면 이동 콜백 호출
- 로그인 성공 이동은 `SessionState.Authenticated` 기반으로 수행

**Step 2: NavHost 라우팅 연결**
- `SignUp` composable에서 `session` 전달
- `onRequireKakaoEmailVerification`에서 안내 route로 이동
- 안내 route composable 추가 및 액션 연결

**Step 3: 컴파일 확인**
Run: `./gradlew :app:compileDevDebugKotlin --no-daemon`  
Expected: BUILD SUCCESSFUL

### Task 5: 분기 누락 보정 및 최종 검증

**Files:**
- Modify: `feature/setting/src/main/java/kr/sjh/setting/screen/SettingScreen.kt` (신규 SessionState 분기 처리)
- Modify: 기타 exhaustive `when(session)` 파일(컴파일 에러 기준)
- Modify: `README.md` (필요 시 동작 설명 추가)

**Step 1: exhaustive 분기 보정**
- `SessionState.EmailVerificationRequired`를 비로그인 계열로 처리

**Step 2: 최종 검증**
Run:
- `./gradlew :app:compileDevDebugKotlin --no-daemon`
- `./gradlew :feature:signIn:compileDebugKotlin --no-daemon`

Expected: BUILD SUCCESSFUL
