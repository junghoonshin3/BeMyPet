package kr.sjh.feature.adoption.navigation

import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.navOptions
import kotlinx.serialization.Serializable

@Serializable
data object Adoption

fun NavController.navigateToAdoption(
    navOptions: NavOptions? = navOptions {
        popUpTo(graph.startDestinationId) { inclusive = true }
        launchSingleTop = true
    }
) {
    navigate(Adoption, navOptions)
}