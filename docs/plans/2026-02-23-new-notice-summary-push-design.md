# 신규 공고 요약 푸시(6시간 주기) 설계

## 목표
공공 API 기반 유기동물 공고에서 신규 항목을 감지해, 사용자 관심 조건에 맞는 결과를 6시간마다 1건 요약 푸시로 발송한다. 공고 원문은 저장하지 않고, 최소 상태만 유지해 무료 티어 비용을 통제한다.

## 제약 및 전제
- 앱은 공공 API를 직접 조회해 공고를 표시하고 있으며, 공고 원문 저장 테이블은 없다.
- 공공 API는 timestamp 단위 조회가 아니라 날짜(YYYYMMDD) 단위 조회 파라미터를 사용한다.
- 신규 감지는 날짜 겹침 조회 + dedupe 키 저장으로 구현한다.
- 발송은 사용자별 다건이 아닌 요약 1건으로 고정한다.

## 아키텍처
- 트리거: GitHub Actions cron (`6시간 주기`)에서 `new_notice_dispatch` Edge Function 호출
- 실행 로직: `new_notice_dispatch`가 공공 API 조회, 신규 감지, 관심사 매칭, FCM 발송, 로그 기록 담당
- 상태 저장(최소):
  - `notification_dispatch_state`: 마지막 성공/실패 시각과 메타 상태
  - `notification_seen_notices`: 이미 처리한 공고 키(`notice_no` 또는 `desertion_no`)만 저장
- 기존 테이블 재사용:
  - `user_interest_profiles`
  - `notification_subscriptions`
  - `notification_delivery_logs`

## 신규 감지 로직
1. `notification_dispatch_state.last_success_date`를 읽는다.
2. 조회 기간을 날짜 단위로 계산한다.
   - 시작일: `last_success_date - 1일`(버퍼)
   - 종료일: `today`
3. 공공 API를 날짜 범위로 페이지네이션 조회한다.
4. 각 공고에서 고유키를 계산한다.
   - 우선순위: `notice_no` -> `desertion_no`
5. `notification_seen_notices`에 `insert ... on conflict do nothing`을 수행한다.
6. 이번 실행에서 실제 insert된 키만 신규 공고로 취급한다.

## 매칭 및 발송 로직
1. 활성 구독을 조회한다.
   - `notification_subscriptions.push_opt_in = true`
   - 토큰 공백 제거
2. 관심사 프로필(`user_interest_profiles`)과 공고를 매칭한다.
   - 지역/축종/성별/크기 조건
   - 비어있는 관심사 필드는 와일드카드로 취급
3. 사용자별 매칭 결과를 집계한다.
4. 사용자당 1건의 요약 푸시를 발송한다.
   - 예: `"관심 조건에 맞는 신규 공고 7건이 등록됐어요"`
   - data payload: `campaign_type=new_animal_summary`, `batch_id`, `matched_count`
5. `notification_delivery_logs`에 `status=sent|failed`를 기록한다.

## 실패 처리 및 복구
- 공공 API 실패:
  - 현재 실행은 중단
  - `notification_dispatch_state.last_error_at/last_error_message` 기록
  - `last_success_date`는 갱신하지 않아 다음 주기에서 재수집 가능
- FCM 실패:
  - 토큰 무효(`UNREGISTERED`, `INVALID_ARGUMENT` 토큰류)면 `notification_subscriptions`에서 토큰 삭제
  - 기타 오류는 `failed` 로그만 남기고 다음 주기 재시도
- 중복 방지:
  - `notification_seen_notices` unique key
  - `notification_delivery_logs.dedupe_key`

## 운영/비용 가드
- 한 실행당 최대 처리 건수 상한(예: 사용자별 요약 발송 5,000건)
- `notification_seen_notices`는 30일 TTL 배치 삭제
- dry-run 모드 유지(운영 검증/스모크 테스트 용도)

## 관측 지표(Firebase Analytics + 서버 로그)
- 앱 이벤트
  - `push_received`
  - `push_opened`
- 서버 지표(집계 소스)
  - `matched_users`
  - `sent_count`
  - `failed_count`
  - `invalid_token_deleted_count`
- 운영 리포트는 `notification_delivery_logs`와 앱 analytics 이벤트를 결합해 확인한다.

## 테스트 전략
- Edge Function 단위 테스트
  - 날짜 범위 계산
  - dedupe 신규 판별
  - 사용자별 요약 집계
  - 실패 코드별 토큰 정리
- 스모크 테스트
  - dry-run 호출에서 `matched_users`/`queued(or planned)` 검증
  - 실제 모드에서 로그 상태 변화 검증
- 워크플로우 검증
  - 수동 `workflow_dispatch`로 함수 호출 성공/실패 경로 확인

## 제외 범위
- 공고 원문 저장 테이블 도입
- 실시간(분 단위) 발송
- 개인별 상세 추천 랭킹 모델
