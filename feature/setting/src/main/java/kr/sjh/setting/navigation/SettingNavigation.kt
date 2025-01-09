package kr.sjh.setting.navigation

import androidx.navigation.NavController
import androidx.navigation.NavOptions
import kotlinx.serialization.Serializable
import kr.sjh.core.model.Screen

@Serializable
data object Setting : Screen

fun NavController.navigateToSetting(
    navOptions: NavOptions? = androidx.navigation.navOptions {
        popUpTo(graph.startDestinationId) { inclusive = true }
        launchSingleTop = true
    }
) {
    navigate(Setting, navOptions)
}