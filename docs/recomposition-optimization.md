# BeMyPet Recomposition 최적화 문서

## 개요

Jetpack Compose에서 불필요한 Recomposition은 UI 성능 저하의 주요 원인입니다.
이 문서는 BeMyPet 프로젝트에서 발견된 Recomposition 원인과 적용된 최적화 기법을 정리합니다.

---

## 1. `collectAsState()` → `collectAsStateWithLifecycle()` 변경

### 파일
- `BeMyPetApp.kt`

### 문제 (Before)
```kotlin
val session by startViewModel.session.collectAsState()
```

`collectAsState()`는 Composable이 Composition에 존재하는 동안 **항상** Flow를 수집합니다. 앱이 백그라운드로 가더라도 Flow 수집이 계속되어, 백그라운드 상태에서도 상태가 변경되면 **불필요한 Recomposition이 발생**합니다.

### 해결 (After)
```kotlin
val session by startViewModel.session.collectAsStateWithLifecycle()
```

`collectAsStateWithLifecycle()`은 **Lifecycle이 STARTED 이상일 때만** Flow를 수집합니다. 앱이 백그라운드에 있으면 수집을 자동 중단하므로 불필요한 Recomposition을 방지하고 리소스도 절약됩니다.

---

## 2. `@Immutable` 어노테이션 추가 (data class 안정성 보장)

### 파일
- `Sido.kt`
- `Sigungu.kt`

### 문제 (Before)
```kotlin
data class Sido(
    val orgCd: String = "",
    val orgdownNm: String = "전국",
)
```

Compose 컴파일러는 data class의 안정성(Stability)을 판단하여 Recomposition 스킵 여부를 결정합니다. 외부 모듈에 위치한 클래스나 컴파일러가 안정성을 추론하지 못하는 경우, **매번 Recomposition 대상으로 판단**됩니다. `Sido`, `Sigungu`는 모든 필드가 `val String`으로 불변이지만, 멀티 모듈 구조에서 컴파일러가 이를 보장하지 못할 수 있습니다.

### 해결 (After)
```kotlin
@Immutable
data class Sido(
    val orgCd: String = "",
    val orgdownNm: String = "전국",
)
```

`@Immutable` 어노테이션을 붙여 Compose 컴파일러에게 "이 클래스의 모든 프로퍼티는 생성 후 변경되지 않는다"고 **명시적으로 알려줍니다**. 이렇게 하면 컴파일러가 해당 타입을 **Stable로 취급**하여, 값이 같으면 Recomposition을 스킵할 수 있습니다.

---

## 3. `MutableState` 프로퍼티 → 불변 프로퍼티로 변경 (Category 클래스)

### 파일
- `AdoptionUiState.kt` (Category data class)
- `FilterViewModel.kt`
- `FilterCategoryList.kt`

### 문제 (Before)
```kotlin
data class Category(
    val type: CategoryType,
    val isSelected: MutableState<Boolean> = mutableStateOf(false),
    val selectedText: MutableState<String> = mutableStateOf(type.title)
) {
    fun reset() {
        isSelected.value = false
        selectedText.value = type.title
    }
}
```

data class 내부에 `MutableState`를 필드로 가지고 있으면 **두 가지 문제**가 발생합니다:

1. **Stability 파괴**: `MutableState`는 내부 값이 언제든 변경 가능하므로 Compose 컴파일러가 이 클래스를 **Unstable**로 판단합니다. 이 타입을 파라미터로 받는 모든 Composable은 **Smart Recomposition(스킵 최적화)을 받을 수 없습니다.**
2. **예측 불가능한 Recomposition**: `MutableState`를 직접 변경(`apply {}`)하면 StateFlow의 `update {}`와 무관하게 **Compose가 별도로 상태 변경을 감지**하여, 의도치 않은 시점에 Recomposition이 발생합니다.

ViewModel에서의 사용도 문제입니다:
```kotlin
// MutableState를 직접 변경 - StateFlow copy와 별개로 Recomposition 트리거
it.selectedCategory?.isSelected?.value = true
it.selectedCategory?.selectedText?.value = neuter.title
```

### 해결 (After)
```kotlin
data class Category(
    val type: CategoryType,
    val isSelected: Boolean = false,
    val selectedText: String = type.title
)
```

모든 필드를 **불변(val) 기본 타입**으로 변경하여 Compose 컴파일러가 **Stable로 판단**할 수 있게 했습니다.

ViewModel에서도 `copy()`를 통해 새 인스턴스를 생성하는 방식으로 변경:
```kotlin
// 불변 copy로 새 인스턴스 생성 - StateFlow를 통해 단일 경로로 Recomposition
it.copy(selectedNeuter = neuter).withUpdatedCategory { cat ->
    cat.copy(isSelected = true, selectedText = neuter.title)
}
```

`withUpdatedCategory` 헬퍼를 도입하여 categoryList 내의 해당 카테고리도 함께 업데이트합니다:
```kotlin
private fun FilterUiState.withUpdatedCategory(
    transform: (Category) -> Category
): FilterUiState {
    val updated = selectedCategory?.let(transform)
    return copy(
        selectedCategory = updated,
        categoryList = if (updated != null) {
            categoryList.map { if (it.type == updated.type) updated else it }
        } else categoryList
    )
}
```

이로써 상태 변경이 **StateFlow → collectAsStateWithLifecycle → Recomposition** 단일 경로로 흐르게 되어, 상태 변경 흐름이 예측 가능해졌습니다.

---

## 4. `rememberUpdatedState` 불필요 사용 제거

### 파일
- `RefreshIndicator.kt`

### 문제 (Before)
```kotlin
val df by rememberUpdatedState(state.distanceFraction)
val refreshing by rememberUpdatedState(isRefreshing)

val resId by remember(state.distanceFraction) {
    derivedStateOf {
        when {
            df > 0f && df <= 0.5f -> { ... }
            ...
        }
    }
}
```

`rememberUpdatedState`는 주로 **오래 실행되는 side effect 안에서 최신 값을 참조**해야 할 때 사용합니다 (예: `LaunchedEffect(Unit)` 내부). 여기서는 그런 용도가 아닌데도 사용하여:

1. **불필요한 State 래핑**: 이미 Composable 파라미터로 받은 값을 다시 State로 감싸, 추가적인 상태 객체가 생성됩니다.
2. **remember의 key와 derivedStateOf 이중 사용**: `remember(state.distanceFraction)`에 key를 지정하면서 내부에서 `derivedStateOf`를 사용하는 것은 모순입니다. key가 변경될 때마다 `derivedStateOf` 블록이 새로 생성되므로 `derivedStateOf`의 캐싱 효과를 잃습니다.

### 해결 (After)
```kotlin
val resId by remember {
    derivedStateOf {
        when {
            state.distanceFraction > 0f && state.distanceFraction <= 0.5f -> { ... }
            ...
        }
    }
}
```

- `rememberUpdatedState` 제거 → 파라미터를 직접 참조
- `remember`에서 key 제거 → `derivedStateOf`가 내부적으로 `state.distanceFraction` 변경을 감지하여 필요할 때만 재계산
- `isRefreshing`도 파라미터를 직접 사용하여 불필요한 State 래핑 제거

---

## 5. Composable 내부에서 매 Recomposition마다 객체 재생성 방지

### 5-1. `RoundedCornerShape` 객체 재생성 방지

#### 파일
- `AdoptionScreen.kt`
- `FavouriteScreen.kt`
- `PetDetailScreen.kt`

#### 문제 (Before)
```kotlin
// Composable 내부에서 호출될 때마다 새 객체 생성
.clip(RoundedCornerShape(10.dp))
.background(Color.Red, RoundedCornerShape(3.dp))
.background(color, RoundedCornerShape(bottomStart = 10.dp, bottomEnd = 10.dp))
```

`RoundedCornerShape(10.dp)`는 호출할 때마다 **새로운 Shape 객체를 생성**합니다. Composable 내부에서 사용되면 Recomposition이 발생할 때마다 새 인스턴스가 만들어져, Modifier 체인이 변경된 것으로 판단되어 **불필요한 레이아웃 재계산**을 유발할 수 있습니다.

#### 해결 (After)
```kotlin
// Shape.kt - 파일 레벨 상수로 한 번만 생성
val RoundedCorner10 = RoundedCornerShape(10.dp)
val RoundedCornerBottom10 = RoundedCornerShape(bottomStart = 10.dp, bottomEnd = 10.dp)
val RoundedCornerTop28 = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
val RoundedCorner3 = RoundedCornerShape(3.dp)

// 사용처에서 상수 참조
.clip(RoundedCorner10)
.background(Color.Red, RoundedCorner3)
```

파일 레벨 상수(top-level `val`)로 선언하여 **앱 전체에서 단일 인스턴스를 재사용**합니다. Recomposition 시에도 동일 참조를 사용하므로 불필요한 객체 생성과 레이아웃 재계산을 방지합니다.

### 5-2. `TextStyle` 객체 재생성 방지

#### 파일
- `AdoptionScreen.kt`
- `FavouriteScreen.kt`

#### 문제 (Before)
```kotlin
@Composable
private fun Pet(modifier: Modifier, pet: Pet) {
    val fontSize = 9.sp
    // Recomposition마다 5번 * 2개 = 10개의 TextStyle 객체 생성
    TextLine(
        titleTextStyle = TextStyle(fontWeight = FontWeight.Bold, fontSize = fontSize),
        contentTextStyle = TextStyle(fontWeight = FontWeight.Light, fontSize = fontSize)
    )
    TextLine(
        titleTextStyle = TextStyle(fontWeight = FontWeight.Bold, fontSize = fontSize),
        contentTextStyle = TextStyle(fontWeight = FontWeight.Light, fontSize = fontSize)
    )
    // ... 반복
}
```

`TextStyle()`은 호출할 때마다 새 객체를 생성합니다. Pet 리스트 아이템 하나당 10개의 TextStyle이 매 Recomposition마다 생성됩니다. 리스트에 20개의 아이템이 있다면 **200개의 불필요한 객체가 매번 생성**됩니다.

#### 해결 (After)
```kotlin
// Composable 외부, 파일 레벨 상수로 선언
private val PetItemTitleStyle = TextStyle(fontWeight = FontWeight.Bold, fontSize = 9.sp)
private val PetItemContentStyle = TextStyle(fontWeight = FontWeight.Light, fontSize = 9.sp)

@Composable
private fun Pet(modifier: Modifier, pet: Pet) {
    TextLine(
        titleTextStyle = PetItemTitleStyle,
        contentTextStyle = PetItemContentStyle
    )
    // ...
}
```

---

## 6. `mutableStateOf` + `by remember` → `remember`만 사용 (불변 객체)

### 파일
- `PetDetailScreen.kt` (ShelterMap)

### 문제 (Before)
```kotlin
val mapProperties by remember {
    mutableStateOf(
        MapProperties(maxZoomPreference = 19f, minZoomPreference = 5f)
    )
}
val mapUiSettings by remember {
    mutableStateOf(
        MapUiSettings(compassEnabled = false, ...)
    )
}
```

`MapProperties`와 `MapUiSettings`는 한 번 생성된 후 **절대 변경되지 않는 불변 값**입니다. 그런데 `mutableStateOf()`로 감싸면:

1. **불필요한 MutableState 객체 생성**: 변경할 일 없는 값을 변경 가능한 State로 감싸 메모리를 낭비합니다.
2. **Compose가 해당 State를 관찰 대상으로 등록**: 실제로 변경되지 않더라도 Composition에서 이 State를 읽는 것으로 기록하여, Recomposition 판단 과정에서 불필요한 체크 대상이 됩니다.

### 해결 (After)
```kotlin
val mapProperties = remember {
    MapProperties(maxZoomPreference = 19f, minZoomPreference = 5f)
}
val mapUiSettings = remember {
    MapUiSettings(compassEnabled = false, ...)
}
```

`remember { ... }`만 사용하여 **단순 캐싱**합니다. `mutableStateOf` 없이도 Recomposition 시 재생성을 막을 수 있고, 불필요한 State 관찰 오버헤드를 제거합니다.

---

## 7. `LaunchedEffect(Unit)` → 적절한 key 사용

### 파일
- `LocaltionContent.kt`

### 문제 (Before)
```kotlin
LaunchedEffect(Unit) {
    sidoState.scrollToItem(sidoIndex)
}
LaunchedEffect(Unit) {
    sigunguState.scrollToItem(sigunguIndex)
}
```

`LaunchedEffect(Unit)`은 **최초 Composition 시 한 번만 실행**됩니다. 사용자가 다른 시도(Sido)를 선택하여 `sidoIndex`가 바뀌어도 스크롤이 갱신되지 않습니다. 이는 Recomposition 최적화 이슈는 아니지만, 잘못된 key 사용으로 인한 **동작 버그**입니다.

### 해결 (After)
```kotlin
LaunchedEffect(sidoIndex) {
    sidoState.scrollToItem(sidoIndex)
}
LaunchedEffect(sigunguIndex) {
    sigunguState.scrollToItem(sigunguIndex)
}
```

`sidoIndex`, `sigunguIndex`를 key로 지정하여 **선택 값이 변경될 때마다 스크롤이 올바르게 갱신**됩니다.

---

## 요약 테이블

| # | 최적화 기법 | 원인 | 해결 |
|---|-----------|------|------|
| 1 | `collectAsStateWithLifecycle` | 백그라운드에서도 Flow 수집 지속 | Lifecycle-aware 수집으로 변경 |
| 2 | `@Immutable` 어노테이션 | 멀티모듈에서 Stability 추론 실패 | 명시적 불변성 선언 |
| 3 | `MutableState` → 불변 필드 | data class 내부 MutableState로 Unstable + 이중 상태 경로 | 불변 필드 + copy() 패턴 |
| 4 | `rememberUpdatedState` 제거 | 불필요한 State 래핑 + derivedStateOf 효과 상실 | 파라미터 직접 참조 + 순수 derivedStateOf |
| 5 | Shape/TextStyle 상수화 | Recomposition마다 동일 객체 반복 생성 | 파일 레벨 상수로 단일 인스턴스 재사용 |
| 6 | `mutableStateOf` 제거 | 불변 값에 불필요한 MutableState 래핑 | `remember`만 사용한 단순 캐싱 |
| 7 | `LaunchedEffect` key 수정 | `Unit` key로 인해 상태 변경 시 미갱신 | 적절한 key 지정으로 동작 버그 수정 |
