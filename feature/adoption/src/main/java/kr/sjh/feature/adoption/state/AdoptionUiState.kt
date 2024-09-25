package kr.sjh.feature.adoption.state

import kr.sjh.core.model.FilterBottomSheetState
import kr.sjh.core.model.FilterCategory
import kr.sjh.core.model.adoption.Pet
import kr.sjh.core.model.adoption.filter.Area
import kr.sjh.core.model.adoption.filter.Kind
import kr.sjh.core.model.adoption.filter.Neuter
import kr.sjh.core.model.adoption.filter.Sido
import kr.sjh.core.model.adoption.filter.Sigungu
import kr.sjh.core.model.adoption.filter.State
import kr.sjh.core.model.adoption.filter.UpKind

sealed class AdoptionFilterCategory : FilterCategory {
    data class DateRange(override val categoryName: String = "기간") : AdoptionFilterCategory()

    data class UpKind(override val categoryName: String = "축종") : AdoptionFilterCategory()

    data class Area(override val categoryName: String = "지역") : AdoptionFilterCategory()

    data class State(override val categoryName: String = "상태") : AdoptionFilterCategory()

    data class Neuter(override val categoryName: String = "중성화 여부") : AdoptionFilterCategory()
}

enum class UpKindOptions(val title: String, val cd: String?) {
    ALL("전체", null), DOG("개", "417000"), CAT("고양이", "422400"), ETC("기타", "429900")
}

enum class StateOptions(val title: String, val value: String?) {
    ALL("전체", null), NOTICE("공고중", "notice"), PROTECT("보호중", "protect")
}

enum class NeuterOptions(val title: String, val value: String?) {
    ALL("전체", null), YES("예", "Y"), NO("아니요", "N"), UNKNOWN("미상", "U")
}

data class AdoptionFilterState(
    val selectedCategories: List<AdoptionFilterCategory> = listOf(),
    val selectedSido: Sido = Sido(),
    val selectedSigungu: Sigungu = Sigungu(),
    val selectedArea: Area = Area(),
    val selectedUpKind: UpKind = UpKind(),
    val selectedState: State = State(),
    val selectedNeuter: Neuter = Neuter(),
    val filterBottomSheetState: FilterBottomSheetState = FilterBottomSheetState.HIDE,
    val kinds: List<Kind> = emptyList(),
    val sidoList: List<Sido> = emptyList(),
    val sigunguList: List<Sigungu> = emptyList(),
)

data class AdoptionUiState(
    val isRefreshing: Boolean = false,
    val isMore: Boolean = false,
    val pets: List<Pet> = emptyList(),
    val pageNo: Int = 1,
)
