# Onboarding Push Permission Design

## Goal
온보딩 마지막 페이지의 "새 공고 푸시 알림 받기" 선택이 실제 권한 요청/저장/서버 동기화로 이어지도록 구현한다.
추가로 설정 화면에서 푸시를 다시 켤 때 권한을 재요청할 수 있게 만든다.

## Scope
- 온보딩 완료 시점 권한 요청 연동
- 권한 거부 시 `pushOptIn=false` 저장
- 설정 화면 푸시 스위치 추가 및 OFF->ON 권한 재요청
- 푸시 동의 변경 시 로컬 저장 + 서버 `notification_subscriptions.push_opt_in` 동기화
- 앱 시작 시 무조건 권한 요청하던 기존 로직 제거

비범위:
- 마케팅/캠페인 메시지 템플릿
- 알림 채널 세분화
- 백엔드 스키마 추가 변경

## Current Context
- 온보딩 스위치 값은 `SettingRepository.updatePushOptIn`으로 저장되지만, 권한 요청은 `StartActivity.onStart()` 전역 1회 체크로 분리되어 있다.
- 설정 화면에는 푸시 ON/OFF 제어 UI가 없다.
- 토큰 업서트는 시작 시점/토큰 갱신 시점 중심으로 동작한다.

## Architecture
1. 온보딩 화면에서 마지막 단계 "시작하기" 클릭 시 권한 요청을 직접 수행한다.
2. 권한 결과를 반영해 `OnboardingViewModel.submit(...)`에 최종 `pushOptIn` 값을 전달한다.
3. 설정 화면에 푸시 스위치를 추가하고, OFF->ON에서 권한이 없으면 즉시 요청한다.
4. 푸시 동의 변경이 확정되면:
   - `SettingRepository.updatePushOptIn(enabled)` 저장
   - 로그인 상태이고 FCM 토큰 확보 시 `NotificationRepository.upsertSubscription(..., pushOptIn=enabled)` 즉시 동기화
5. `StartActivity`의 전역 권한 팝업 로직은 제거해 온보딩/설정 중심 UX로 전환한다.

## UX Rules
- 온보딩
  - `pushOptIn=false`: 권한 요청 없이 완료
  - `pushOptIn=true` + 권한 이미 허용: 그대로 완료
  - `pushOptIn=true` + 권한 미허용: 권한 요청
    - 허용: `true` 저장
    - 거부: `false`로 저장 후 완료
- 설정
  - ON->OFF: 즉시 OFF 반영
  - OFF->ON:
    - 권한 허용 상태면 즉시 ON 반영
    - 권한 미허용이면 요청 후 결과 반영
      - 허용: ON
      - 거부: OFF 유지 + 안내 스낵바

## Data Flow
1. Onboarding Complete
   - UI가 권한 상태를 판별/요청
   - 최종 bool(`resolvedPushOptIn`)을 ViewModel에 전달
   - ViewModel이 설정 저장 + 관심 프로필 업서트

2. Setting Toggle
   - UI가 스위치 액션 처리
   - 필요 시 권한 요청 후 최종 bool 결정
   - ViewModel이 설정 저장
   - 로그인 사용자면 현재 FCM 토큰으로 구독 row upsert

3. App Start
   - 기존 권한 요청 없음
   - 세션/토큰 동기화는 기존 흐름 유지

## Error Handling
- 권한 거부: 기능 흐름은 계속 진행, `pushOptIn=false`로 안전하게 저장
- FCM 토큰 조회 실패: 로컬 저장은 유지, 서버 동기화는 다음 기회에 재시도
- Supabase 업서트 실패: UI에 메시지 표시하고 앱 흐름 중단 없음

## Testing Strategy
- Unit
  - `OnboardingViewModel`: 권한 결과 override 반영 저장 검증
  - `SettingViewModel`(신규/확장): 푸시 동의 저장 및 서버 동기화 호출 검증
- Manual QA
  - Android 13+ 에뮬레이터에서 온보딩 ON/거부/허용 시나리오
  - 설정 OFF->ON 재요청 시나리오
  - 로그인/비로그인 각각에서 동작 확인

## Trade-offs
- 권한 요청을 온보딩/설정으로 옮기면 UX 맥락은 좋아지지만 구현 지점이 분산된다.
- 즉시 서버 동기화는 정확성을 높이지만 토큰 조회 실패 시 지연 반영이 남는다(허용 가능한 범위).
