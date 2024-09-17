package kr.sjh.feature.adoption.state

import kr.sjh.core.model.adoption.Pet

enum class AlertBottomSheetState {
    NOTHING, SHOW, HIDE
}

data class AdoptionUiState(
    val isLoading: Boolean = false,
    val pets: List<Pet>? = null,
    val bottomSheetState: AlertBottomSheetState = AlertBottomSheetState.NOTHING
)