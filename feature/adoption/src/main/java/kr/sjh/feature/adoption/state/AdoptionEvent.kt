package kr.sjh.feature.adoption.state

import kr.sjh.core.model.FilterBottomSheetState
import kr.sjh.core.model.adoption.filter.Area
import kr.sjh.core.model.adoption.filter.Neuter
import kr.sjh.core.model.adoption.filter.Sido
import kr.sjh.core.model.adoption.filter.Sigungu
import kr.sjh.core.model.adoption.filter.State
import kr.sjh.core.model.adoption.filter.UpKind

sealed interface AdoptionEvent {
    data object Refresh : AdoptionEvent
    data object LoadMore : AdoptionEvent
    data class SelectedCategory(val category: AdoptionFilterCategory) : AdoptionEvent
    data class SelectedSido(val sido: Sido) : AdoptionEvent
    data class SelectedSigungu(val sigungu: Sigungu) : AdoptionEvent
    data class SelectedUpKind(val upKind: UpKind) : AdoptionEvent
    data class SelectedArea(val area: Area) : AdoptionEvent
    data class SelectedState(val state: State) : AdoptionEvent
    data class SelectedNeuter(val neuter: Neuter) : AdoptionEvent
    data class FilterBottomSheetOpen(val bottomSheetState: FilterBottomSheetState) : AdoptionEvent

    data object Apply : AdoptionEvent
}