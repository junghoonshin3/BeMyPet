package kr.sjh.feature.adoption_detail.state

import kr.sjh.core.model.adoption.Pet


sealed class DetailUiState {
    data object Loading : DetailUiState()
    data class Success(val pet: Pet) : DetailUiState()
    data class Failure(val e: Throwable) : DetailUiState()
}