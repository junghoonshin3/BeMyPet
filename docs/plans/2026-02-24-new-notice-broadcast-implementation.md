# New Notice Broadcast Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** 관심사 매칭을 제거하고 신규 공고 발생 시 푸시 수신 동의 사용자 전체에게 요약 푸시를 발송한다.

**Architecture:** `new_notice_dispatch`의 사용자 필터를 구독 기반으로 단순화한다. 관심사 조회/매칭 루프를 제거하고, `new_notices`가 존재할 때 구독 사용자별 summary를 직접 생성해 FCM을 발송한다. 푸시 문구와 README를 새 동작에 맞게 동기화한다.

**Tech Stack:** Supabase Edge Functions (Deno/TypeScript), FCM HTTP v1, Supabase PostgREST

---

### Task 1: 관심사 기반 매칭 제거 및 구독 기반 summary 생성

**Files:**
- Modify: `supabase/functions/new_notice_dispatch/index.ts`

**Step 1: Remove interest-specific imports and types**
- `matchesInterest` import 제거
- `InterestProfileRow` 타입 및 관심사 조회 함수 제거

**Step 2: Build summaries from subscription users**
- `tokenMap`에서 사용자 ID 목록을 추출
- `new_notices`가 있으면 사용자별 `noticeKeys`를 동일하게 부여
- `matchedCount = new_notices.length`로 설정

**Step 3: Keep response contract but update semantics**
- `matched_users` 필드 유지
- 값은 “발송 대상 사용자 수”로 계산

**Step 4: Verify compile**
Run: `deno check supabase/functions/new_notice_dispatch/index.ts`
Expected: PASS

### Task 2: 푸시 문구 변경

**Files:**
- Modify: `supabase/functions/new_notice_dispatch/fcm_client.ts`

**Step 1: Update notification body text**
- 기존 관심사 문구 제거
- 본문을 `신규 유기동물 공고 {N}건이 등록됐어요.`로 변경

**Step 2: Verify compile**
Run: `deno check supabase/functions/new_notice_dispatch/fcm_client.ts`
Expected: PASS

### Task 3: 문서 업데이트

**Files:**
- Modify: `supabase/functions/new_notice_dispatch/README.md`

**Step 1: Update behavior description**
- 관심사 매칭 표현 제거
- 구독 기반 전체 브로드캐스트 설명 반영

**Step 2: Update troubleshooting**
- `matched_users=0` 원인을 “신규 공고 없음/구독 대상 없음” 중심으로 정리

### Task 4: 검증 및 커밋

**Step 1: Run validation commands**
Run:
- `deno check supabase/functions/new_notice_dispatch/index.ts`
- `deno check supabase/functions/new_notice_dispatch/fcm_client.ts`

Expected: PASS

**Step 2: Git commit**
```bash
git add supabase/functions/new_notice_dispatch/index.ts \
  supabase/functions/new_notice_dispatch/fcm_client.ts \
  supabase/functions/new_notice_dispatch/README.md \
  docs/plans/2026-02-24-new-notice-broadcast-design.md \
  docs/plans/2026-02-24-new-notice-broadcast-implementation.md
git commit -m "feat(dispatch): broadcast new notices without interest filtering"
```
