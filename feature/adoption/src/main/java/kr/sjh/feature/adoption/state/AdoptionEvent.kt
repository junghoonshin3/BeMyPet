package kr.sjh.feature.adoption.state

import kr.sjh.core.ktor.model.request.PetRequest
import kr.sjh.core.model.adoption.filter.Sido
import kr.sjh.core.model.adoption.filter.Sigungu

sealed interface AdoptionEvent {
    data class Refresh(val req: PetRequest) : AdoptionEvent
    data class LoadMore(val req: PetRequest) : AdoptionEvent
}


sealed interface FilterEvent {
    data object Reset : FilterEvent
    data class SelectedCategory(val category: Category) : FilterEvent
    data class FetchSigungu(val sido: Sido) : FilterEvent
    data class ConfirmLocation(val sido: Sido, val sigungu: Sigungu) : FilterEvent
    data object CloseBottomSheet : FilterEvent
    data object OpenBottomSheet : FilterEvent
    data class ConfirmNeuter(val neuter: Neuter) : FilterEvent
    data class ConfirmUpKind(val upkind: UpKind) : FilterEvent
    data class ConfirmDateRange(val startDate: String, val endDate: String) : FilterEvent
}
