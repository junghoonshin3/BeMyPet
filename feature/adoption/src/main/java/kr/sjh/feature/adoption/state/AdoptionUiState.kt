package kr.sjh.feature.adoption.state

import kr.sjh.core.model.FilterBottomSheetState
import kr.sjh.core.model.FilterCategory
import kr.sjh.core.model.adoption.Pet
import kr.sjh.core.model.adoption.filter.DateRange
import kr.sjh.core.model.adoption.filter.Location
import kr.sjh.core.model.adoption.filter.Option
import kr.sjh.core.model.adoption.filter.Sido
import kr.sjh.core.model.adoption.filter.Sigungu


data class AdoptionCategory(
    val categoryName: String,
    val displayName: String
)

enum class Category(override val categoryName: String, override val displayName: String) :
    FilterCategory {
    DATE_RANGE("기간", "기간"), LOCATION("지역", "지역"), UP_KIND("축종", "축종"), STATE(
        "상태", "상태"
    ),
    NEUTER("중성화 여부", "중성화 여부")
}

sealed class FilterOption {
    data class OneListOption(val options: List<Option>) : FilterOption()
    data class TwoTextFieldOption(val option1: String, val option2: String) : FilterOption()
    data class TwoListOption(val option1: List<Option>, val option2: List<Option>) : FilterOption()
}

enum class UpKindOptions(override val title: String, override val value: String?) : Option {
    ALL("전체", null), DOG("개", "417000"), CAT("고양이", "422400"), ETC("기타", "429900")
}

enum class StateOptions(override val title: String, override val value: String?) : Option {
    ALL("전체", null), NOTICE("공고중", "notice"), PROTECT("보호중", "protect")
}

enum class NeuterOptions(override val title: String, override val value: String?) : Option {
    ALL("전체", null), YES("예", "Y"), NO("아니요", "N"), UNKNOWN("미상", "U")
}


data class AdoptionFilterState(
    val categories: Map<FilterCategory, FilterOption> = mapOf(
        Category.DATE_RANGE to FilterOption.TwoTextFieldOption(
            DateRange().startDate, DateRange().endDate
        ),
        Category.LOCATION to FilterOption.TwoListOption(listOf(), listOf()),
        Category.UP_KIND to FilterOption.OneListOption(UpKindOptions.entries),
        Category.STATE to FilterOption.OneListOption(StateOptions.entries),
        Category.NEUTER to FilterOption.OneListOption(NeuterOptions.entries),
    ),
    val selectedUpKind: UpKindOptions = UpKindOptions.ALL,
    val selectedState: StateOptions = StateOptions.ALL,
    val selectedNeuter: NeuterOptions = NeuterOptions.ALL,
    val selectedDateRange: DateRange = DateRange(),
    val selectedLocation: Location = Location(),
    val filterBottomSheetState: FilterBottomSheetState = FilterBottomSheetState.HIDE,
    val selectedCategory: List<FilterCategory> = emptyList(),
    val sidoList: List<Sido> = emptyList(),
    val sigunguList: List<Sigungu> = emptyList(),
)

data class AdoptionUiState(
    val isRefreshing: Boolean = false,
    val isMore: Boolean = false,
    val pets: List<Pet> = emptyList(),
    val totalCount: Int = 0
)
