# BeMyPet 디자인 가이드라인 v1

- Last Updated: 2026-02-16
- Scope: `홈(입양)`, `관심`, `설정`, 공통 `테마/하단탭/칩`
- Base Branch: `feat/design-concept-reference`

---

## 1. 가이드 목적

BeMyPet의 핵심 과업인 **입양 탐색 효율**을 유지하면서, 앱의 첫인상과 탐색 경험을 **따뜻하고 신뢰감 있는 반려동물 커뮤니티 톤**으로 통일한다.

핵심 원칙:
- 빠른 탐색: 정보 우선순위를 분명히 한다.
- 친근한 톤: 베이지/오렌지 계열로 감성 일관성을 만든다.
- 중간 밀도: 카드는 핵심 정보만, 상세는 상세 화면으로 위임한다.

---

## 2. 브랜드 무드

키워드:
- 따뜻함
- 생활 밀착
- 보호/신뢰
- 간결한 커뮤니티

피해야 할 방향:
- 지나치게 기술적이거나 차가운 톤
- 과한 장식으로 정보 가독성을 해치는 레이아웃
- 강조 색상 남발로 피로를 유발하는 UI

---

## 3. 디자인 토큰

### 3.1 Color Tokens

Light:
- `Primary`: `#FFF4E8`
- `OnPrimary`: `#2F2A24`
- `PrimaryContainer`: `#FFE2C6`
- `OnPrimaryContainer`: `#4C3623`
- `Secondary`: `#F4A259`
- `OnSecondary`: `#3A250F`
- `SecondaryContainer`: `#FFF0E0`
- `OnSecondaryContainer`: `#5B3A1E`
- `Background`: `#FFFBF7`
- `OnBackground`: `#2F2A24`
- `Surface`: `#FFFFFF`
- `OnSurface`: `#2F2A24`
- `SurfaceVariant`: `#FFF7EE`
- `OnSurfaceVariant`: `#7C6A59`
- `Outline`: `#E7D7C6`
- `Error`: `#D84C4C`
- `OnError`: `#FFFFFF`

Dark:
- `Primary`: `#2B221A`
- `OnPrimary`: `#FFEBD2`
- `PrimaryContainer`: `#3B2D21`
- `OnPrimaryContainer`: `#FADBB8`
- `Secondary`: `#D79A5C`
- `OnSecondary`: `#311E0A`
- `SecondaryContainer`: `#4A3727`
- `OnSecondaryContainer`: `#FFDDB6`
- `Background`: `#1D1712`
- `OnBackground`: `#F6E5D4`
- `Surface`: `#241C16`
- `OnSurface`: `#F6E5D4`
- `SurfaceVariant`: `#2D231B`
- `OnSurfaceVariant`: `#CBB7A4`
- `Outline`: `#5C4A3A`
- `Error`: `#FF8C7A`
- `OnError`: `#3F0B05`

### 3.2 Typography

- `headlineSmall`: 24sp / SemiBold / letterSpacing 0
- `titleMedium`: 16sp / SemiBold
- `bodyMedium`: 14sp / Medium
- `bodySmall`: 12sp / Medium
- `labelLarge`: 13sp / SemiBold
- `labelMedium`: 12sp / SemiBold
- `labelSmall`: 11sp / SemiBold

### 3.3 Shape & Spacing

- 기본 스페이싱 스케일: `4 / 8 / 12 / 16 / 20 / 24`
- 칩 라운드: `12dp`
- 카드 라운드: `18dp`
- 상단/하단 바 라운드: `24dp`
- 확장 앱바 높이: `176dp`
- 기본 그림자: `2dp` (카드), `4dp` (상단/하단바)

---

## 4. 컴포넌트 가이드

### 4.1 Top App Bar (확장형)

구성:
- 컨텍스트 문구 1줄 (`labelMedium`)
- 화면 타이틀 1줄 (`headlineSmall`)
- 보조 설명 1줄 (`bodySmall`, 선택)
- 하단 칩 영역

규칙:
- 배경은 `Primary`, 텍스트는 `OnPrimary`/`OnPrimaryContainer`
- 스크롤 시 접히더라도 필터 칩은 접근 가능해야 함

### 4.2 Bottom Navigation

구성:
- 둥근 컨테이너형 (`RoundedCorner24`)
- 선택 탭 인디케이터는 `SecondaryContainer`
- 선택 텍스트/아이콘: `OnSurface`
- 비선택 텍스트/아이콘: `OnSurfaceVariant`

규칙:
- 라벨은 항상 노출한다.
- 탭 간 시각적 무게를 균등하게 유지한다.

### 4.3 Filter Chips

기본 상태:
- 배경 `Surface`
- 보더 `Outline`
- 텍스트 `OnSurfaceVariant`

선택 상태:
- 배경 `SecondaryContainer`
- 보더 `Secondary`
- 텍스트 `OnSecondaryContainer`

리셋 버튼:
- 동일 높이(40dp)와 동일 라운드(12dp) 유지

### 4.4 Pet Card

구성:
- 상단 이미지(고정 높이)
- 핵심 텍스트 2줄(품종/발견장소)
- 메타 칩 2개(성별/상태)
- 공고번호 1줄(`labelSmall`)

필수:
- 긴 텍스트는 `ellipsis`
- 카드 높이는 리스트 내 균질 유지
- 상태 배지(`공고중`)는 이미지 상단 좌측에 배치

### 4.5 Settings Section Card

구성:
- 섹션 타이틀
- 관련 액션 버튼/항목 그룹

규칙:
- 기본 액션은 `SurfaceVariant`/중립 색상
- 위험 액션(회원탈퇴)만 `Error` 계열 사용

---

## 5. 화면별 가이드

### 5.1 홈(입양)

목표:
- 필터 기반 탐색의 속도와 신뢰감 강화

규칙:
- 헤더에 컨텍스트 문구 + 타이틀 + 보조 설명 배치
- 카드 간격을 12dp 이상 확보해 가독성 유지
- 빈 상태는 중앙 카드형 메시지로 안내

### 5.2 관심

목표:
- 저장한 동물 목록을 명확하게 재탐색 가능하게 제공

규칙:
- 홈과 동일 카드 시스템 재사용
- 상단 타이틀은 “내가 저장한 친구들” 맥락 제공
- 빈 상태에서는 홈 탭 행동을 유도하는 문구 사용

### 5.3 설정

목표:
- 기능 목록을 카드 단위로 묶어 인지 부하 감소

규칙:
- 테마/프로필/계정/정책을 독립 섹션 카드로 구분
- 버튼 높이 50~54dp 범위 유지
- 경고 액션만 빨강 사용

---

## 6. 카피라이팅 가이드

톤:
- 친근하지만 과장되지 않게
- 짧고 행동 중심 문장 사용

권장:
- “필터로 조건을 빠르게 좁혀보세요”
- “입양 탭에서 마음에 드는 친구를 저장해보세요”

지양:
- 기술 용어 중심 문장
- 책임 회피형 모호한 안내

---

## 7. 접근성/사용성 기준

- 텍스트 대비: 라이트/다크 모두 본문 가독성 확보
- 터치 영역: 최소 40dp 이상(권장 44dp+)
- 상태 표현: 색상만 의존하지 않고 텍스트 병행
- 리스트 성능: 동일 카드 구조 유지, 불필요한 레이아웃 변형 최소화

---

## 8. v2 확장 범위 (앱 전반)

- Last Updated: 2026-02-16
- Scope: `상세`, `댓글`, `신고`, `로그인/온보딩`, `차단`
- 정책: `Email/Password는 UI-only`, `온보딩은 최초 1회`

### 8.1 상세(입양) 화면

- 섹션 카드 레이아웃 적용:
  - 히어로 이미지
  - 핵심 요약(품종/성별/상태/공고번호)
  - 발견/공고 정보
  - 보호소 정보 + 지도
  - 기타 메타 정보
- 댓글 진입 CTA는 카드로 노출한다.
- 긴 텍스트(`발견장소`, `품종`, `보호장소`)는 maxLines + ellipsis를 사용한다.

### 8.2 댓글 화면

- 댓글 아이템 액션은 `단일 More 버튼`으로 통합한다.
- 액션시트 메뉴 규칙:
  - 본인/관리자: `수정`, `삭제`
  - 타인: `댓글 신고`, `사용자 신고`, `사용자 차단`
- 신고 타입 선택은 액션시트 내부 선택으로 단계를 줄인다.

### 8.3 신고 화면

- 드롭다운 중심 레이아웃을 카드형 폼으로 정리한다.
- 사유 선택/추가 설명/제출 버튼의 위계를 명확히 유지한다.
- IME 노출 시 제출 버튼 접근성이 유지되도록 배치한다.

### 8.4 로그인/온보딩

- 로그인 탭 구성:
  - `Google`: 기존 인증 플로우 유지
  - `Email`: 입력 UI 제공 + 제출 비활성 + 준비중 문구
- 온보딩은 3단계 요약형으로 제공한다.
- 최초 1회 완료/건너뛰기 이후에는 재노출하지 않는다.

### 8.5 차단 화면

- 사용자 목록은 카드 행 + 우측 `차단 해제` 버튼으로 구성한다.
- 전체 카드 클릭 해제 패턴을 사용하지 않는다.
- 빈 상태는 중앙 카드형 안내 메시지를 사용한다.

---

## 8. 모션/인터랙션 기준

- 하단탭: 기본 시스템 전환 애니메이션 유지
- 상단 헤더: 스크롤 연동 오프셋 애니메이션(과도한 바운스 금지)
- 버튼/칩: 클릭 시 즉시 상태 반영, 지연형 피드백 금지

---

## 9. QA 체크리스트

기능:
- 필터 선택/해제 상태가 시각적으로 명확한가
- 빈 상태 화면이 모든 탭에서 자연스러운가
- 하단탭 선택 상태가 라우트와 정확히 일치하는가

레이아웃:
- 360dp, 412dp+ 폭에서 카드/칩 줄바꿈이 안정적인가
- 상태바/네비게이션바 패딩이 중복되지 않는가

시각:
- 라이트/다크에서 동일한 브랜드 톤을 유지하는가
- 위험 액션 색상 사용 범위가 제한되어 있는가

---

## 10. 구현 파일 맵 (v1)

- Theme
  - `core/designsystem/src/main/java/kr/sjh/core/designsystem/theme/Color.kt`
  - `core/designsystem/src/main/java/kr/sjh/core/designsystem/theme/Type.kt`
  - `core/designsystem/src/main/java/kr/sjh/core/designsystem/theme/Shape.kt`
- Common Components
  - `core/designsystem/src/main/java/kr/sjh/core/designsystem/components/RoundedCornerButton.kt`
  - `core/designsystem/src/main/java/kr/sjh/core/designsystem/components/CheckBoxButton.kt`
- Navigation
  - `app/src/main/java/kr/sjh/bemypet/navigation/BeMyPetBottomNavigation.kt`
- Screens
  - `feature/adoption/src/main/java/kr/sjh/feature/adoption/screen/AdoptionScreen.kt`
  - `feature/adoption/src/main/java/kr/sjh/feature/adoption/screen/filter/FilterCategoryList.kt`
  - `feature/favourite/src/main/java/kr/sjh/feature/favourite/screen/FavouriteScreen.kt`
  - `feature/setting/src/main/java/kr/sjh/setting/screen/SettingScreen.kt`

---

## 11. 버전 운영 규칙

- 컴포넌트 변경 시 먼저 본 문서 토큰/규칙을 갱신한다.
- 화면 단위 예외를 둘 경우 이유와 적용 범위를 문서에 명시한다.
- v2에서는 `상세/댓글/로그인` 화면을 동일 규칙으로 확장한다.
