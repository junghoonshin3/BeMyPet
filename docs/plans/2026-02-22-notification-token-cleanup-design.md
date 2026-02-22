# Notification Token Cleanup Design

## Goal
`notification_subscriptions` 테이블에서 사용하지 않는 FCM 토큰을 자동 정리한다.  
정책은 `행 삭제`이며, 아래 두 경로를 지원한다.

- 즉시 삭제: 영구 실패 토큰(`UNREGISTERED`, `INVALID_ARGUMENT`) 전달 시 삭제
- 배치 삭제: `30일` 이상 비활성 토큰 삭제

## Current Context
- 현재 `notification_subscriptions`는 `fcm_token`이 유니크이고 `user_id`는 유니크가 아니다.
- 따라서 사용자의 토큰이 바뀌면 새 row가 추가될 수 있으며, 정리 정책이 없으면 row가 누적된다.
- 현재 `new_notice_dispatch`는 발송 로그만 기록하고 실제 FCM 발송을 하지 않으므로 즉시 실패 토큰 감지는 아직 훅만 준비한다.

## Proposed Architecture
새 Edge Function `notification_token_cleanup`를 추가한다.

- 입력 모드
  - `mode = "stale"`: 30일 비활성 토큰 삭제
  - `mode = "invalid"`: 전달된 `invalid_tokens` 목록 삭제
- 공통 옵션
  - `dry_run` (default: true)
  - `stale_before_days` (default: 30, `mode=stale`에서만 사용)
- 인증
  - 서비스 호출자만 허용(`service_role` 키 또는 service_role JWT)
- DB 접근
  - service role client로만 `notification_subscriptions` 삭제

## Data Flow
### 1) Stale Cleanup
1. 요청 수신 (`mode=stale`)
2. 기준 시각 계산 (`now() - stale_before_days`)
3. `coalesce(last_active_at, updated_at, created_at)`가 기준보다 오래된 row 대상 조회
4. `dry_run=false`면 대상 row delete
5. 삭제 건수/대상 건수 반환

### 2) Invalid Token Cleanup
1. 요청 수신 (`mode=invalid`, `invalid_tokens`)
2. 토큰 normalize(trim, blank 제거, distinct)
3. 대상 row 조회
4. `dry_run=false`면 해당 토큰 row delete
5. 삭제 건수/대상 건수 반환

## Error Handling
- 필수 필드 누락/모드 불일치: `400`
- 인증 실패: `401`
- DB 작업 실패: `500` + 메시지
- 정상: `200` + `{ mode, dry_run, matched_count, deleted_count }`

## Testing Strategy
- Python smoke test `supabase/scripts/notification_token_cleanup_smoke_test.py`
  - stale 모드: 31일 전 데이터 삽입 후 삭제 확인
  - invalid 모드: 토큰 목록 전달 후 삭제 확인
  - dry_run 모드: 삭제 없이 건수만 반환 확인
- 함수 배포 후 실환경 smoke 실행

## Rollout
1. 함수 배포
2. 일일 실행 스케줄러 연결(새벽 1회, `mode=stale`, `stale_before_days=30`, `dry_run=false`)
3. 향후 실제 FCM 발송 파이프라인 도입 시, 영구 실패 토큰을 `mode=invalid`로 즉시 삭제 연결

## Trade-offs
- 장점: 누적 토큰 자동 정리, DB 오염 방지, 정책 명시적
- 단점: 스케줄러 운영 포인트 추가, 즉시 삭제는 발송 파이프라인 연동 전까지 수동/테스트 호출에 한정

