# Supabase Google Auth 회원/댓글 개편 로드맵

- Last Updated: 2026-02-15
- Branch: `feat/supabase-auth-profile-comment-refactor`
- Commit: `3be4e86`
- PR: https://github.com/junghoonshin3/BeMyPet/pull/34

## 현재 상태
- Supabase 프로젝트 `lvmcycuhrgqgdmxwdnmy`에 CLI `db push`로 마이그레이션 적용 완료 (2026-02-15)
- 다음 작업: 권한/앱 시나리오 검증 수행

## 목표
- `auth.users` + `public.profiles` 기반 회원 관리 표준화
- `comment_feed` 기반 댓글 조회 일원화
- 차단/권한 필터를 DB(RLS)에서 강제

## 진행 체크리스트

### 1) 스키마/정책 설계 및 코드 반영
- [x] `profiles`, `comments`, `blocks`, `reports` 개편 SQL 작성
- [x] `comment_feed`, `block_feed` 뷰 정의
- [x] RLS 정책/트리거/함수(`nickname cooldown`, `updated_at`, `on_auth_user_created`) 반영
- [x] Android 모델/리포지토리/서비스/UI를 `profiles`/`comment_feed` 기준으로 전환

### 2) 마이그레이션 배포
- [x] Supabase 프로젝트(`lvmcycuhrgqgdmxwdnmy`)에 `supabase/migrations/20260216_profiles_comments_auth_refactor.sql` 적용 (2026-02-15, `supabase db push`)
- [ ] 기존 운영 데이터 정합성 검증 (`notice_no`, `user_id` FK, soft delete)

### 3) 권한/보안 검증
- [ ] 본인 외 댓글 수정/삭제 불가 확인
- [ ] 차단 사용자 댓글 비노출 확인(RLS)
- [ ] 닉네임 중복/쿨다운 위반 차단 확인
- [ ] 프로필 이미지 버킷 경로 권한 확인

### 4) 앱 기능 검증
- [ ] Google 로그인 직후 `profiles` 자동 생성 확인
- [ ] 댓글 목록에 작성자 닉네임/아바타 실시간 반영 확인
- [ ] 차단 후 목록 재조회 시 즉시 반영 확인
- [ ] 설정 화면 닉네임/아바타 수정 동작 확인

### 5) 후속 정리
- [ ] 안정화 후 `comments.raw_user_meta_data` 제거
- [ ] 안정화 후 `blocks.raw_user_meta_data` 제거
- [ ] Edge Function(`delete_user`, `banned_until`) soft-delete 호환 수정

## 이미 검증된 항목
- [x] `./gradlew :core:model:testDebugUnitTest :core:supabase:testDebugUnitTest :core:data:testDebugUnitTest :feature:comments:testDebugUnitTest :feature:block:testDebugUnitTest :feature:setting:testDebugUnitTest`
- [x] `./gradlew :app:compileDevDebugKotlin`

## 운영 규칙 (항상 업데이트)
- 항목 완료 시 체크박스(`- [x]`)로 변경
- 완료 라인 끝에 날짜와 근거를 추가 (예: ` (2026-02-15, SQL Editor 적용)`) 
- 범위 변경 시 본 문서의 `목표`와 `진행 체크리스트`를 먼저 갱신
