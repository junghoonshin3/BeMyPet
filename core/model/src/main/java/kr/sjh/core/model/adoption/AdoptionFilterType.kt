package kr.sjh.core.model.adoption

import kr.sjh.core.model.FilterType

enum class AdoptionFilterType(override val filterName: String) : FilterType {
    DateRanged("종류"),// 축종코드
    UpKind("축종"), // 개,고양이,기타
    Kind("품종"), // 그레이 하운드, 골든 리트리버 등
    Area("지역"), // 지역
    State("상태"), // 상태
    Neuter("중성화 여부") // 중성화 여부
}