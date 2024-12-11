package kr.sjh.feature.adoption_detail.state


sealed interface AdoptionDetailEvent {
    data object AddLike : AdoptionDetailEvent
    data object RemoveLike : AdoptionDetailEvent

}