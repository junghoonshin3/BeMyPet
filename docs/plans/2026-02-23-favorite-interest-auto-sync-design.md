# 관심목록 기반 관심사 자동 동기화 설계

## 배경
- 현재 `new_notice_dispatch`는 `user_interest_profiles.regions/species/sexes/sizes`로 신규 공고를 매칭한다.
- 앱의 관심목록(`favourite_pet`)은 로컬 DB에만 저장되고 서버 관심사로 자동 반영되지 않는다.
- 그 결과 사용자가 관심목록을 꾸준히 추가해도 서버 매칭 필터는 비어 있거나 오래된 상태로 남을 수 있다.

## 목표
- 관심목록에 등록된 펫 정보를 기반으로 사용자 관심사를 자동 추론한다.
- 추론된 관심사를 서버의 기존 관심사와 합집합으로 병합해 `user_interest_profiles`를 갱신한다.
- 동기화 시점은 관심목록 변경 직후 + 앱 시작 시 1회로 구성해 누락을 복구한다.

## 비목표
- 사용자 수동 설정 UI(온보딩/설정)의 구조 변경
- 서버 함수(`new_notice_dispatch`) 매칭 로직 자체 변경
- 관심사 제거를 위한 차집합/감쇠 모델 도입(이번 범위는 합집합 병합만)

## 결정 사항
1. 병합 정책: `기존 서버 관심사 ∪ 관심목록 추론 관심사`
2. 동기화 시점
- 즉시: 관심목록 추가/삭제 성공 직후
- 보정: 로그인 사용자의 앱 시작 시 1회
3. 실패 처리
- 동기화 실패가 관심목록 추가/삭제 UX를 막지 않도록 비차단 처리
- 로그를 남기고 다음 동기화 시점에서 재시도

## 관심사 추론 규칙
### 지역(regions)
- 소스: `noticeNo` 접두 문자열 (`"전남-함평-..."` 형태)
- 방식: 접두(예: `전남`, `서울`)를 시도 코드(`6460000`, `6110000`)로 매핑
- 매칭 스키마: `new_notice_dispatch`가 `uprCd` 우선 비교하므로 시도 코드로 저장

### 종(species)
- 우선 규칙: `upKindCode`가 존재하면 코드 매핑
  - `417000 -> dog`
  - `422400 -> cat`
  - 기타는 원 코드값(lowercase)
- 보조 규칙: 코드가 없으면 품종 문자열(`kindFullName/kindName/kindCode`) 휴리스틱
  - `개/견` 포함 -> `dog`
  - `고양이/묘` 포함 -> `cat`

### 성별(sexes)
- `M/F/Q` 중 유효값만 사용

### 크기(sizes)
- `weight`의 숫자 추출 후 서버와 동일 기준
  - `<= 5`: `SMALL`
  - `<= 15`: `MEDIUM`
  - `> 15`: `LARGE`

## 아키텍처
### 데이터 계층 확장
- `NotificationService`/`NotificationRepository`에 `getInterestProfile(userId)` 추가
- 서버 기존 관심사를 조회한 뒤, 앱에서 계산한 추론 관심사와 병합해 `upsertInterestProfile` 수행

### 도메인 유틸
- `FavoriteInterestProfileDeriver`(신규)
- 입력: `List<Pet>`
- 출력: `DerivedInterestProfile(regions/species/sexes/sizes)`
- 단일 책임: 추론 규칙 캡슐화 + 중복 제거

### 동기화 조정자
- `InterestProfileSyncCoordinator`(신규, data 모듈)
- 의존: `FavouriteRepository`, `NotificationRepository`, `SettingRepository`
- 기능
  - 로그인 사용자의 관심목록을 읽어 추론
  - 기존 서버 관심사 조회
  - 합집합 병합
  - `push_enabled`는 로컬 push 설정값 기준 유지

### 호출 지점
- 관심목록 즉시 동기화: `PetDetailViewModel`의 관심 추가/삭제 성공 후
- 앱 시작 보정 동기화: `StartViewModel`에서 로그인 상태일 때 1회 실행

## 데이터 흐름
1. 사용자가 관심목록 추가/삭제
2. 로컬 DB 반영 성공
3. 세션 사용자 식별 가능하면 동기화 조정자 실행
4. 로컬 관심목록 -> 관심사 추론
5. 서버 기존 관심사 조회
6. 합집합 병합 -> `user_interest_profiles` upsert

앱 시작 시에는 3~6단계를 보정성으로 1회 수행한다.

## 에러 처리
- 동기화 실패 시 로컬 즐겨찾기 변경은 롤백하지 않음
- 에러 로그에 사용자 id, 동기화 단계(조회/병합/upsert) 명시
- 세션 없음/사용자 id 공백이면 조용히 스킵

## 테스트 전략
1. `FavoriteInterestProfileDeriverTest`
- 지역/종/성별/크기 추론 규칙 검증
- 중복 제거 및 공백값 무시 검증

2. `InterestProfileMergeTest`
- 서버 기존값 + 추론값 합집합 병합 검증
- push_enabled 유지 규칙 검증

3. ViewModel/Coordinator 테스트
- 관심 추가/삭제 성공 시 동기화 호출 검증
- 앱 시작 시 로그인 상태에서 1회 동기화 호출 검증

## 롤아웃/검증
- 개발 환경에서 관심목록 추가/삭제 후 `user_interest_profiles` 컬럼 변화 확인
- `new_notice_dispatch(dry_run=false)` 실행 시 `matched_users` 증가 추적
- Firebase Analytics 이벤트(향후): interest_sync_success/failure 추가 여지
