# New Notice Broadcast Design

## Goal
관심사 매칭을 임시 제거하고, 신규 공고가 발생하면 푸시 수신 동의 사용자 전체에게 요약 푸시를 발송한다.

## Scope
- 서버 함수 `new_notice_dispatch`의 타겟팅 기준을 관심사 기반에서 구독 기반으로 단순화
- 푸시 문구를 일반 신규 공고 안내 문구로 변경
- 운영/개발 스모크 관점에서 `matched_users`가 0으로 고정되는 문제를 완화

## Non-Goals
- 앱의 관심사 수집/동기화 코드 제거
- DB 스키마 삭제 (`user_interest_profiles`는 유지)
- 개인화 추천/세분화 타겟팅 고도화

## Current Problem
현 구조는 아래 조건을 동시에 만족해야 발송 대상이 된다.
1. `user_interest_profiles.push_enabled = true`
2. `notification_subscriptions.push_opt_in = true`
3. 신규 공고가 관심사(region/species/sex/size)와 AND 매칭

실환경에서는 관심사 데이터 공백/포맷 불일치로 `matched_users=0`이 자주 발생해 발송이 되지 않는다.

## Proposed Design
1. 발송 대상 계산 단순화
- `notification_subscriptions`에서 `push_opt_in = true`인 사용자/토큰만 대상으로 삼는다.
- 신규 공고(`new_notices`)가 1건 이상이면 대상 사용자 전원에게 동일 요약 푸시를 보낸다.

2. 집계값 의미 정렬
- `matched_users`는 “관심사 매칭 사용자 수”가 아니라 “실제 발송 대상 사용자 수” 의미로 사용한다.
- `matched_count`는 사용자별 동일하게 `new_notice_count`를 사용한다.

3. 문구 변경
- Title: `새 공고 알림`
- Body: `신규 유기동물 공고 {N}건이 등록됐어요.`

## Data Flow
1. 공공 API/수동 payload에서 공고 수집
2. `notification_seen_notices`로 신규 공고만 추출
3. 구독 ON 사용자 토큰 맵 구성
4. 신규 공고가 있으면 사용자별 summary 생성(모든 신규 공고 key 포함)
5. FCM 발송 + delivery log 기록

## Risks & Mitigation
- 리스크: 개인화 제거로 알림량 증가
- 완화: 기존 `MAX_TOKEN_SEND_PER_RUN` 제한 유지, 이후 관심사 기능 재도입 시 feature flag 고려

## Validation
- 함수 수동 호출(`dry_run=true/false`)에서 `new_notice_count > 0`일 때 `matched_users > 0` 확인
- 구독 사용자 1명 이상일 때 `target_token_count`와 `sent_count` 증가 확인
- README 문서 기준/트러블슈팅 문구 동기화 확인
