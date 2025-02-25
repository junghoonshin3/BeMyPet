package kr.sjh.feature.adoption.state

import kr.sjh.core.model.adoption.filter.Sido
import kr.sjh.core.model.adoption.filter.Sigungu
import java.time.LocalDate

sealed interface AdoptionEvent {
    data object Refresh : AdoptionEvent
    data object LoadMore : AdoptionEvent
}


sealed interface FilterEvent {
    data object Reset : FilterEvent
    data class SelectedCategory(val category: Category) : FilterEvent
    data class SelectedSido(val sido: Sido) : FilterEvent
    data class SelectedSigungu(val sigungu: Sigungu) : FilterEvent
    data class SelectedNeuter(val neuter: Neuter) : FilterEvent
    data class SelectedUpKind(val upKind: UpKind) : FilterEvent
    data object CloseBottomSheet : FilterEvent
    data object OpenBottomSheet : FilterEvent
}
