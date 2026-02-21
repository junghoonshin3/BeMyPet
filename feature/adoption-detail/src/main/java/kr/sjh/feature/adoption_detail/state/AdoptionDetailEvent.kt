package kr.sjh.feature.adoption_detail.state

sealed interface AdoptionDetailEvent {
    data class OnFavorite(val isFavorite: Boolean) : AdoptionDetailEvent
    data object ToggleCompare : AdoptionDetailEvent
}
