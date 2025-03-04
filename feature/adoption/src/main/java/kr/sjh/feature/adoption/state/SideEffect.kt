package kr.sjh.feature.adoption.state

sealed class SideEffect {
    data object HideBottomSheet : SideEffect()
    data object ShowBottomSheet : SideEffect()
    data object FetchPets : SideEffect()
}