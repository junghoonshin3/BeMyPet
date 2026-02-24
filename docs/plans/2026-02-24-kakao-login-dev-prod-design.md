# Kakao Login (dev/prod 분리) Design

## 배경
- 현재 로그인은 Google ID Token 기반만 동작하고, Kakao 버튼은 준비중 상태이다.
- 앱은 `devDebug`/`prodRelease` 2개 변형만 빌드되며, Supabase도 환경별로 분리되어 있다.
- 목표는 Kakao 로그인을 Supabase Auth 기반으로 추가하고, dev/prod 환경을 안전하게 분리하는 것이다.

## 목표
- Kakao 로그인 버튼이 실제 로그인 플로우를 수행한다.
- `dev`와 `prod`가 각각 별도 Kakao 앱 설정(및 Supabase 프로젝트 설정)을 사용한다.
- Kakao 계정에 이메일이 없거나 검증되지 않은 경우 로그인 완료를 막고 안내 메시지를 제공한다.

## 아키텍처 결정
- 인증 방식은 Supabase Auth OAuth(`auth.signInWith(Kakao)`)를 사용한다.
- Android 딥링크 콜백은 `StartActivity`에서 `SupabaseClient.handleDeeplinks(intent)`로 처리한다.
- Supabase Auth 설정에 deeplink scheme/host를 명시해 PKCE 콜백 경로를 고정한다.

## 환경 분리 전략
- 딥링크 URI를 환경별로 분리:
  - `devDebug`: `bemypet-dev://oauth`
  - `prodRelease`: `bemypet://oauth`
- `AndroidManifest.xml`의 OAuth intent-filter는 manifest placeholder(`AUTH_SCHEME`, `AUTH_HOST`)로 치환한다.
- Supabase 설정은 기존처럼 env별 `SUPABASE_URL`/`SUPABASE_ANON_KEY`를 사용하고, Kakao provider 설정은 각 Supabase 프로젝트(dev/prod)에서 별도로 관리한다.

## 구성 요소 변경
- `core/supabase`
  - `AuthService`/`AuthServiceImpl`: `signInWithKakao` 추가
  - Kakao 로그인 후 `currentUserOrNull()` 검사:
    - `email` 비어있음 또는 `emailConfirmedAt == null`이면 `auth.signOut(SignOutScope.LOCAL)` 후 실패 처리
  - `SupabaseModule`: `Auth` config에 `scheme`, `host` 설정
  - `build.gradle.kts`: `SUPABASE_AUTH_SCHEME`, `SUPABASE_AUTH_HOST` BuildConfig 추가
- `core/data`
  - `AuthRepository`/`AuthRepositoryImpl`: `signInWithKakao` 위임 메서드 추가
- `feature/signIn`
  - Kakao 버튼 클릭 시 실제 로그인 호출
  - ViewModel에 `onKakaoSignIn()` 추가, 로딩/에러 상태 공통 처리
  - 실패 메시지 매핑은 provider별(google/kakao)로 분리
- `app`
  - `AndroidManifest.xml` OAuth data scheme/host placeholder 적용
  - `app/build.gradle.kts`에 `AUTH_SCHEME`, `AUTH_HOST` placeholder 주입
  - `StartActivity`에서 `handleDeeplinks` 호출(onCreate/onNewIntent)

## 데이터/흐름
1. 사용자가 Kakao 버튼 탭
2. `SignInViewModel.onKakaoSignIn()` -> `AuthRepository.signInWithKakao()`
3. Supabase OAuth 페이지/브라우저 로그인
4. 앱 딥링크(`bemypet(-dev)://oauth?...code=...`) 복귀
5. `handleDeeplinks`가 code 교환 후 세션 저장
6. `AuthServiceImpl.signInWithKakao`가 이메일 검증
7. 성공 시 `isSignedIn=true`, 실패 시 에러 노출

## 에러 처리
- OAuth 취소/실패: 일반 로그인 실패 메시지 노출
- 이메일 미제공/미검증: 전용 안내 메시지 노출
- Supabase provider 미설정: 서버 오류 메시지를 래핑해 노출
- 딥링크 미처리: 세션 미생성으로 실패하므로 로그를 남기고 재시도 안내

## 테스트 전략
- 단위 테스트:
  - `SignInViewModel`의 Kakao 성공/실패 상태 전이
  - provider별 메시지 매핑
- 통합 검증:
  - `:app:compileDevDebugKotlin`
  - `:app:compileProdReleaseKotlin`
- 수동 검증:
  - dev/prod 각각 Kakao 로그인 버튼 -> 브라우저 -> 앱 복귀 -> 로그인 완료
  - 이메일 없는 Kakao 계정 시 실패 및 안내 문구 확인
