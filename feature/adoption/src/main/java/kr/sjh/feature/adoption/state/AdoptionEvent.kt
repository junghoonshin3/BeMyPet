package kr.sjh.feature.adoption.state

import kr.sjh.core.model.adoption.filter.DateRange
import kr.sjh.core.model.adoption.filter.Sido
import kr.sjh.core.model.adoption.filter.Sigungu
import kr.sjh.feature.adoption.screen.filter.Category
import java.time.LocalDate

sealed interface AdoptionEvent {
    data object Refresh : AdoptionEvent
    data object LoadMore : AdoptionEvent
    data class SelectedCategory(val category: Category) : AdoptionEvent
    data object InitCategory : AdoptionEvent
    data class SelectedLocation(val sido: Sido, val sigungu: Sigungu) : AdoptionEvent
    data class SelectedNeuter(val neuter: Neuter) : AdoptionEvent
    data class SelectedUpKind(val upKind: UpKind) : AdoptionEvent
    data class LoadSigungu(val sido: Sido) : AdoptionEvent
    data object LoadSido : AdoptionEvent
    data class SelectedDateRange(val startDate: LocalDate, val endDate: LocalDate) : AdoptionEvent

}