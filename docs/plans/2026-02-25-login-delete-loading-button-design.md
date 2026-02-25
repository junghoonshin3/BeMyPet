# 로그인/회원탈퇴 버튼 로딩 피드백 설계

## 배경
- 로그인(구글/카카오), 회원탈퇴는 네트워크 의존 작업이라 응답 대기 시간이 존재한다.
- 현재는 대기 상태에서 버튼 중복 탭이 가능하고, 사용자 입장에서 진행 여부가 불명확하다.

## 목표
- 로그인/회원탈퇴 액션 시작 시 버튼 자체에 즉시 로딩 피드백을 보여준다.
- 로딩 중 중복 액션을 막는다.
- 기존 성공/실패/세션 만료 처리 흐름은 유지한다.

## 범위
- 포함
  - 로그인 화면: Google/Kakao 버튼 로딩 표시
  - 설정 화면: 회원탈퇴 다이얼로그 확인 후 다이얼로그 닫고, 계정 카드의 회원탈퇴 버튼 로딩 표시
- 제외
  - 로그아웃/프로필수정 버튼의 별도 로딩 UX
  - 전역 풀스크린 로딩 오버레이

## 설계 원칙
- 상태 단일화: ViewModel 상태를 단일 소스로 사용한다.
- 최소 변경: 기존 에러 메시지 및 네비게이션 분기를 유지한다.
- 재진입 안정성: 액션 종료 시 로딩 플래그를 확실히 해제한다.

## 상태 모델
### 로그인
- `SignInUiState`에 `loadingProvider: LoadingProvider?`를 추가한다.
- `LoadingProvider`는 `Google`, `Kakao`를 가진다.
- 동작
  - Google 시작 시 `loadingProvider = Google`
  - Kakao 시작 시 `loadingProvider = Kakao`
  - 성공/실패/취소 시 `loadingProvider = null`

### 설정(회원탈퇴)
- `ProfileUiState`에 `isDeletingAccount: Boolean`를 추가한다.
- 동작
  - 다이얼로그 확인 클릭 시 `isDeletingAccount = true`
  - 탈퇴 성공/실패/세션 만료 처리 후 `isDeletingAccount = false`

## UI 동작
### 로그인 버튼
- 로딩 대상 버튼 텍스트를 `처리 중...`으로 변경하고 스피너를 표시한다.
- 두 로그인 버튼 모두 비활성화한다.
- 기존 화면 하단 "로그인 진행 중..." 보조 문구는 제거한다(버튼 자체 피드백으로 대체).

### 회원탈퇴 버튼
- 다이얼로그 확인 시 다이얼로그는 즉시 닫는다.
- 설정 카드의 `회원탈퇴` 버튼을 `처리 중...` + 스피너로 전환한다.
- 처리 중에는 `로그아웃`, `프로필 수정`, `회원탈퇴` 재클릭을 비활성화한다.

## 오류/예외 처리
- 로그인 실패 시 기존 스낵바 메시지 로직 유지.
- 회원탈퇴 실패 시 기존 스낵바 노출 유지.
- 세션 만료 메시지 분기(`DELETE_ACCOUNT_SESSION_EXPIRED_MESSAGE`) 유지.
- 모든 실패 케이스에서 로딩 플래그를 해제한다.

## 영향 파일
- `feature/signIn/src/main/java/kr/sjh/feature/signup/SignInViewModel.kt`
- `feature/signIn/src/main/java/kr/sjh/feature/signup/SignInScreen.kt`
- `feature/setting/src/main/java/kr/sjh/setting/screen/SettingViewModel.kt`
- `feature/setting/src/main/java/kr/sjh/setting/screen/SettingScreen.kt`

## 검증 계획
- 컴파일: `./gradlew :app:compileDevDebugKotlin --no-daemon`
- 수동 테스트
  1. Google 버튼 클릭 시 Google 버튼 로딩 + 두 버튼 비활성화 확인
  2. Kakao 버튼 클릭 시 Kakao 버튼 로딩 + 두 버튼 비활성화 확인
  3. 로그인 실패 후 버튼 상태 원복 확인
  4. 회원탈퇴 다이얼로그 확인 후 다이얼로그 즉시 닫힘 확인
  5. 설정 카드 회원탈퇴 버튼 로딩/비활성화 확인
  6. 회원탈퇴 실패/성공 후 버튼 상태 원복 확인
