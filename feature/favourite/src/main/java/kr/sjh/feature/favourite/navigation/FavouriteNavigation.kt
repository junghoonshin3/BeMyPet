package kr.sjh.feature.favourite.navigation

import androidx.navigation.NavController
import androidx.navigation.NavOptions
import kotlinx.serialization.Serializable
import kr.sjh.core.model.adoption.Pet

@Serializable
data object Favourite

fun NavController.navigateToFavourite(navOptions: NavOptions? = null) {
    navigate(Favourite, navOptions)
}
