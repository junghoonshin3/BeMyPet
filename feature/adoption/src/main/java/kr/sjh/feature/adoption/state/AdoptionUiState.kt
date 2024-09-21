package kr.sjh.feature.adoption.state

import kr.sjh.core.ktor.model.request.AbandonmentPublicRequest
import kr.sjh.core.model.adoption.Pet

enum class AlertBottomSheetState {
    NOTHING, SHOW, HIDE
}

data class AdoptionUiState(
    val isRefreshing: Boolean = false,
    val isMore: Boolean = false,
    val pets: List<Pet>? = null,
    val pageNo: Int = 1,
    val bottomSheetState: AlertBottomSheetState = AlertBottomSheetState.NOTHING
)