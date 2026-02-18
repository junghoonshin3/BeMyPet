package kr.sjh.feature.adoption.state

import kr.sjh.core.ktor.model.request.PetRequest

sealed class SideEffect {
    data object HideBottomSheet : SideEffect()
    data object ShowBottomSheet : SideEffect()
    data class FetchPets(val req: PetRequest) : SideEffect()
}
