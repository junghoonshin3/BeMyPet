package kr.sjh.feature.adoption.state

sealed interface AdoptionEvent {
    data object Refresh : AdoptionEvent
}