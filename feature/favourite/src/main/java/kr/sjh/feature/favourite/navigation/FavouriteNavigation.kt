package kr.sjh.feature.favourite.navigation

import androidx.navigation.NavController
import androidx.navigation.NavOptions
import kotlinx.serialization.Serializable

@Serializable
data object Favourite

//fun NavController.navigateToPetDetail(
//    navOptions: NavOptions? = androidx.navigation.navOptions {
//        popUpTo(graph.startDestinationId) { inclusive = true }
//        launchSingleTop = true
//    }
//) {
//    navigate(Favourite, navOptions)
//}