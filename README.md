<div align="center">
  <img width="115" src="https://github.com/user-attachments/assets/8f2deb82-28ad-49f5-b442-73c0902dd4e7" alt="BeMyPet Logo">
  <h2>나의 펫이 되어줘 (BeMyPet)</h2>
  <p>유기동물 공고를 조회하고, 관심 동물을 저장하며, 신규 공고 알림을 받을 수 있는 Android 앱</p>
  <p>
    <img src="https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white" alt="Android">
    <img src="https://img.shields.io/badge/Jetpack%20Compose-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white" alt="Jetpack Compose">
    <img src="https://img.shields.io/badge/Kotlin-B125EA?style=for-the-badge&logo=kotlin&logoColor=white" alt="Kotlin">
    <img src="https://img.shields.io/badge/Supabase-3FCF8E?style=for-the-badge&logo=supabase&logoColor=white" alt="Supabase">
    <img src="https://img.shields.io/badge/Firebase-F57C00?style=for-the-badge&logo=firebase&logoColor=white" alt="Firebase">
  </p>
</div>

## 앱 링크

- Google Play: https://play.google.com/store/apps/details?id=kr.sjh.bemypet

## 주요 기능

- 공공 API 기반 유기동물 공고 목록/상세 조회
- 조건 기반 필터링, 이미지 확대/축소
- 즐겨찾기(찜) 저장 및 관리
- 댓글, 신고, 차단 기능
- Google 로그인 기반 사용자 계정/프로필 관리
- 신규 공고 요약 푸시 알림 수신
  - 서버 배치(`new_notice_dispatch`)가 6시간 주기로 신규 공고를 감지
  - `push_opt_in=true` 구독 사용자 대상으로 요약 푸시 전송

## 기술 스택

- Android: Kotlin, Jetpack Compose, Navigation Compose
- Architecture: Multi-module + MVVM, Hilt DI
- Data/Network: Ktor, Supabase (Auth/PostgREST/Functions), Kotlin Serialization
- Push/Analytics: Firebase Messaging, Firebase Analytics, Crashlytics
- Backend/Infra: Supabase Edge Functions (Deno), GitHub Actions

## 외부 데이터/API

- 농림축산식품부 농림축산검역본부 국가동물보호정보시스템 구조동물 조회 서비스  
  https://www.data.go.kr/data/15098931/openapi.do

## 프로젝트 구조

```text
app/                  Android app
core/                 공통 모듈(data, supabase, datastore, common ...)
feature/              화면/도메인 단위 기능 모듈
supabase/
  functions/          Edge Functions
  migrations/         DB migration
docs/                 운영 문서/계획 문서
```

## 로컬 개발 시작

### 1) 필수 환경

- Android Studio 최신 안정 버전
- JDK 17
- Android SDK (프로젝트 설정 기준 `compileSdk=36`, `minSdk=24`)

### 2) 필수 로컬 파일 준비

아래 파일이 없으면 Gradle 설정 단계에서 빌드가 중단됩니다.

- `secrets.dev.properties`
- `secrets.prod.properties`
- `secrets.properties`
- `version.properties`
- `app/src/dev/google-services.json`
- `app/src/prod/google-services.json`

`secrets.dev.properties`/`secrets.prod.properties`는 예제 파일을 복사해서 시작하세요.

```bash
cp secrets.dev.properties.example secrets.dev.properties
cp secrets.prod.properties.example secrets.prod.properties
```

워크트리(`git worktree`)를 사용할 경우, 위 비추적 파일들은 자동 복사되지 않으니 각 worktree에 별도로 준비해야 합니다.

### 3) 빌드/테스트

```bash
# 개발 빌드
./gradlew :app:assembleDevDebug

# 단위 테스트
./gradlew :app:testDebugUnitTest
```

## Supabase Functions 운영

주요 함수:

- `new_notice_dispatch`: 신규 공고 요약 푸시 발송
- `notification_token_cleanup`: 미활성/무효 토큰 정리

배포 예시:

```bash
supabase functions deploy new_notice_dispatch
supabase functions deploy notification_token_cleanup
```

상세 절차는 아래 문서를 참고하세요.

- `docs/new-notice-dispatch-runbook.md`
- `docs/notification-token-cleanup-runbook.md`

## 브랜치/배포 흐름

- 기본 흐름: `feature/* -> develop -> main`
- `main`은 `develop`을 통해서만 반영되도록 PR 가드 워크플로우를 사용
- 스케줄 배치 워크플로우:
  - `.github/workflows/new-notice-dispatch.yml`
  - `.github/workflows/notification-token-cleanup.yml`

## 트러블슈팅

- `Missing secrets file: secrets.dev.properties`
  - 루트(worktree 포함)에 `secrets.dev.properties`/`secrets.prod.properties`/`version.properties` 존재 여부 확인
- `File google-services.json is missing`
  - `app/src/dev/google-services.json`, `app/src/prod/google-services.json` 경로 확인
- `branch is already used by worktree`
  - 동일 브랜치가 다른 worktree에서 체크아웃 중인지 `git worktree list`로 확인

## 스크린샷

<img alt="Screenshot_20241211_192307" height="200" src="https://github.com/user-attachments/assets/bbb0c033-0c2e-4973-8d44-05543b1e7adf" width="100"/>
<img alt="Screenshot_20241211_192252" height="200" src="https://github.com/user-attachments/assets/b9af7069-fb4f-4589-a114-a3dc6de7756c" width="100"/>
<img alt="Screenshot_20241211_192232" height="200" src="https://github.com/user-attachments/assets/1a5d4fe7-210a-4f8f-8a4f-20412d479f83" width="100"/>
<img alt="Screenshot_20241211_192152" height="200" src="https://github.com/user-attachments/assets/3c4a4840-c9ca-4326-a988-499da15dd311" width="100"/>
<img alt="Screenshot_20241211_192129" height="200" src="https://github.com/user-attachments/assets/4bf1f014-84a1-4e67-b754-d790aadea9da" width="100"/>
<img alt="Screenshot_20241211_192106" height="200" src="https://github.com/user-attachments/assets/39b496c7-83fd-4fd9-839f-cc76e2521705" width="100"/>
<img alt="Screenshot_20241211_192339" height="200" src="https://github.com/user-attachments/assets/9db55e9d-7bf0-43b8-8cac-19c8dd391286" width="100"/>

## 데모 영상

https://github.com/user-attachments/assets/c3b4cbfa-d73d-4b5e-b3c1-0edf841efff7
