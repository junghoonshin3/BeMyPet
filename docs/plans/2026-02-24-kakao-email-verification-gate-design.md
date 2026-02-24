# Kakao 이메일 확인 가이드 화면 Design

## 배경
- Kakao OAuth는 딥링크 콜백 이후에 세션이 확정된다.
- 기존에는 로그인 시작 직후 검증하려다 PKCE verifier 이슈가 발생했다.
- 요구사항은 이메일 미확인 사용자(이메일 없음/미인증)를 별도 화면으로 안내하고, 앱 설정으로 이동시키는 것이다.

## 목표
- Kakao 로그인 완료 후 사용자 상태를 판별해 2가지 케이스를 분기한다.
  - `NO_EMAIL`: 이메일 정보 없음
  - `UNVERIFIED_EMAIL`: 이메일 미인증
- 조건 불충족 시 일반 로그인 완료로 진입하지 않고 전용 안내 화면을 표시한다.
- 안내 화면에는 `앱 설정 열기`, `다시 로그인` 액션을 제공한다.

## 아키텍처
- 판단 시점: `AuthServiceImpl.getSessionFlow()`의 `SessionStatus.Authenticated` 분기.
- 상태 전달: `SessionState`에 `EmailVerificationRequired` 상태를 추가해 UI로 전달.
- 라우팅: `SignInRoute`가 `session`을 관찰해 `EmailVerificationRequired`이면 전용 화면으로 네비게이션.

## UI/UX
- 화면 제목: `이메일 확인이 필요해요`
- 공통 설명: `카카오 계정의 이메일 확인이 완료되어야 로그인이 가능해요.`
- 케이스별 설명:
  - 이메일 없음: `카카오 계정에 이메일 정보가 없어요.`
  - 이메일 미인증: `카카오 계정 이메일 인증이 아직 완료되지 않았어요.`
- 버튼:
  - `앱 설정 열기` -> `ACTION_APPLICATION_DETAILS_SETTINGS`
  - `다시 로그인` -> 로컬 세션 정리 후 로그인 화면 복귀

## 데이터 흐름
1. Kakao OAuth callback으로 세션 생성
2. `getSessionFlow()`에서 provider/email/emailConfirmed 검사
3. 조건 불충족이면 `SessionState.EmailVerificationRequired(reason)` 반환
4. `SignInRoute`가 감지 후 전용 안내 화면으로 이동
5. 사용자가 `다시 로그인` 선택 시 세션 정리 후 `SignUp` 화면 유지

## 에러 처리
- provider 식별 실패 시 기존 흐름 유지(차단하지 않음)
- 앱 설정 이동 인텐트 실패 시 스낵바로 실패 안내
- 로그인 화면 중복 네비게이션 방지: `launchSingleTop` 적용

## 테스트
- `AuthServiceImpl`에서 Kakao + 이메일 없음/미인증 시 `EmailVerificationRequired` 분기 단위 검증
- 네비게이션 컴파일/회귀 검증:
  - `:app:compileDevDebugKotlin`
  - `:feature:signIn:compileDebugKotlin`
