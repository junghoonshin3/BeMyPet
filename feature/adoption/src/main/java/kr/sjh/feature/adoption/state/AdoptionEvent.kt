package kr.sjh.feature.adoption.state

import kr.sjh.core.model.FilterBottomSheetState
import kr.sjh.core.model.FilterCategory
import kr.sjh.core.model.adoption.filter.DateRange
import kr.sjh.core.model.adoption.filter.Location
import kr.sjh.core.model.adoption.filter.Option

sealed interface AdoptionEvent {
    data object Refresh : AdoptionEvent
    data object LoadMore : AdoptionEvent
    data class SelectedCategory(val category: FilterCategory) : AdoptionEvent
    data class SelectedLocation(val location: Location, val fetchDate: Boolean = false) :
        AdoptionEvent

    data class SelectedUpKind(val upKind: UpKindOptions) : AdoptionEvent
    data class SelectedState(val state: StateOptions) : AdoptionEvent
    data class SelectedNeuter(val neuter: NeuterOptions) : AdoptionEvent
    data class SelectedDateRange(val dateRange: DateRange) : AdoptionEvent
    data class FilterBottomSheetOpen(val bottomSheetState: FilterBottomSheetState) : AdoptionEvent
    data object SelectedInit : AdoptionEvent
    data object Apply : AdoptionEvent
    data class SetLastScrollIndex(val index: Int) : AdoptionEvent
}