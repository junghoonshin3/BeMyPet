package kr.sjh.feature.adoption.state

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import kr.sjh.core.ktor.model.request.PetRequest
import kr.sjh.core.model.adoption.Pet
import kr.sjh.core.model.adoption.filter.Sido
import kr.sjh.core.model.adoption.filter.Sigungu
import kr.sjh.feature.adoption.screen.filter.CategoryType
import java.time.LocalDate
import java.time.format.DateTimeFormatter

enum class UpKind(val title: String, val value: String) {
    ALL("전체", ""), DOG("개", "417000"), CAT("고양이", "422400"), ETC("기타", "429900")
}

enum class Neuter(val title: String, val value: String) {
    ALL("전체", ""), YES("예", "Y"), NO("아니요", "N"), UNKNOWN("미상", "U")
}

val dateRangeFormater = DateTimeFormatter.ofPattern("yyyyMMdd")

data class AdoptionFilterState(
//    val filterList: List<Category> = listOf(
//        Category(DATE_RANGE), Category(NEUTER), Category(UP_KIND), Category(LOCATION)
//    ),
    val selectedCategory: Category? = null,
    val selectedSido: Sido = Sido(),
    val selectedSigungu: Sigungu = Sigungu(),
    val selectedStartDate: LocalDate = LocalDate.now().minusDays(7),
    val selectedEndDate: LocalDate = LocalDate.now(),
    val selectedNeuter: Neuter = Neuter.ALL,
    val selectedUpKind: UpKind = UpKind.ALL,
    val sidoList: List<Sido> = emptyList(),
    val sigunguList: List<Sigungu> = emptyList(),
    val pageNo: Int = 1
) {
    private val dateTimeFormat = DateTimeFormatter.ofPattern("yyyyMMdd")

    fun toAbandonmentPublicRequest(): PetRequest {
        return PetRequest(
            upkind = selectedUpKind.value,
            neuter_yn = selectedNeuter.value,
            bgnde = selectedStartDate.format(dateTimeFormat),
            endde = selectedEndDate.format(dateTimeFormat),
            upr_cd = selectedSido.orgCd,
            org_cd = selectedSigungu.orgCd,
            pageNo = pageNo
        )
    }
}

data class FilterUiState(
    val isLoading: Boolean = false,
    val categoryList: List<Category> = listOf(
        Category(CategoryType.DATE_RANGE),
        Category(CategoryType.NEUTER),
        Category(CategoryType.UP_KIND),
        Category(CategoryType.LOCATION)
    ),
    val selectedCategory: Category? = null,
    val selectedSido: Sido = Sido(),
    val selectedSigungu: Sigungu = Sigungu(),
    val selectedStartDate: String = LocalDate.now().minusDays(7).format(dateRangeFormater)
        .toString(),
    val selectedEndDate: String = LocalDate.now().format(dateRangeFormater).toString(),
    val selectedNeuter: Neuter = Neuter.ALL,
    val selectedUpKind: UpKind = UpKind.ALL,
    val sidoList: List<Sido> = emptyList(),
    val sigunguList: List<Sigungu> = emptyList(),
    val errorMsg: String = ""
) {
    fun toPetRequest(): PetRequest {
        return PetRequest(
            upkind = selectedUpKind.value,
            neuter_yn = selectedNeuter.value,
            bgnde = selectedStartDate,
            endde = selectedEndDate,
            upr_cd = selectedSido.orgCd,
            org_cd = selectedSigungu.orgCd
        )
    }
}

data class AdoptionUiState(
    val isRefreshing: Boolean = false,
    val isMore: Boolean = false,
    val pets: List<Pet> = emptyList(),
    val totalCount: Int = 0,
)

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