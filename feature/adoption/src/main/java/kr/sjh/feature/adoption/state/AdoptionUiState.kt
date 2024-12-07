package kr.sjh.feature.adoption.state

import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kr.sjh.core.ktor.model.request.AbandonmentPublicRequest
import kr.sjh.core.model.adoption.Pet
import kr.sjh.core.model.adoption.filter.DateRange
import kr.sjh.core.model.adoption.filter.Sido
import kr.sjh.core.model.adoption.filter.Sigungu
import kr.sjh.feature.adoption.screen.filter.Category
import kr.sjh.feature.adoption.screen.filter.CategoryType.DATE_RANGE
import kr.sjh.feature.adoption.screen.filter.CategoryType.LOCATION
import kr.sjh.feature.adoption.screen.filter.CategoryType.NEUTER
import kr.sjh.feature.adoption.screen.filter.CategoryType.UP_KIND
import java.time.format.DateTimeFormatter

enum class UpKind(val title: String, val value: String?) {
    ALL("전체", null), DOG("개", "417000"), CAT("고양이", "422400"), ETC("기타", "429900")
}

enum class Neuter(val title: String, val value: String?) {
    ALL("전체", null), YES("예", "Y"), NO("아니요", "N"), UNKNOWN("미상", "U")
}


data class AdoptionFilterState(
    val filterList: List<Category> = listOf(
        Category(DATE_RANGE), Category(NEUTER), Category(UP_KIND), Category(LOCATION)
    ),
    val selectedCategory: Category? = null,
    val selectedSido: Sido = Sido(),
    val selectedSigungu: Sigungu = Sigungu(),
    val selectedDateRange: DateRange = DateRange(),
    val selectedNeuter: Neuter = Neuter.ALL,
    val selectedUpKind: UpKind = UpKind.ALL,
    val sidoList: List<Sido> = emptyList(),
    val sigunguList: List<Sigungu> = emptyList(),
    val pageNo: Int = 1,
    val isLocationError: Boolean = false,
    val isLocationLoading: Boolean = false,
) {
    private val dateTimeFormat = DateTimeFormatter.ofPattern("yyyyMMdd")

    fun toAbandonmentPublicRequest(): AbandonmentPublicRequest {
        return AbandonmentPublicRequest(
            upkind = selectedUpKind.value,
            neuter_yn = selectedNeuter.value,
            bgnde = selectedDateRange.startDate.format(
                dateTimeFormat
            ),
            endde = selectedDateRange.endDate.format(
                dateTimeFormat
            ),
            upr_cd = selectedSido.orgCd,
            org_cd = selectedSigungu.orgCd,
            pageNo = pageNo
        )
    }
}

data class AdoptionUiState(
    val isRefreshing: Boolean = false,
    val isMore: Boolean = false,
    val pets: List<Pet> = emptyList(),
    val totalCount: Int = 0,
    val lastScrollIndex: Int = 0,
    val openBottomSheet: Boolean = false,
    val openDatePicker: Boolean = false,
)
