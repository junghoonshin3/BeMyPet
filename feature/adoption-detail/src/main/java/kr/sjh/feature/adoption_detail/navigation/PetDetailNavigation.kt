package kr.sjh.feature.adoption_detail.navigation

import androidx.compose.runtime.Stable
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import kotlinx.serialization.Serializable
import kr.sjh.core.model.Screen
import kr.sjh.core.model.adoption.Pet

@Serializable
@Stable
data class PetDetail(
    val petInfo: Pet
) : Screen

fun NavController.navigateToPetDetail(petDetail: PetDetail, navOptions: NavOptions? = null) {
    navigate(petDetail, navOptions)
}