package kr.sjh.feature.adoption_detail.state

import kr.sjh.core.model.adoption.Pet


sealed interface AdoptionDetailEvent {
    data class OnFavorite(val isFavorite: Boolean) : AdoptionDetailEvent
}