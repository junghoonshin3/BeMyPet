package kr.sjh.bemypet

import android.content.res.Resources
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navOptions
import kr.sjh.bemypet.navigation.TopLevelDestination
import kr.sjh.feature.adoption.navigation.Adoption
import kr.sjh.feature.chat.navigation.Chat
import kr.sjh.feature.mypage.navigation.MyPage
import kr.sjh.feature.review.navigation.Review

class BeMyPetAppState(
    val navController: NavHostController,
    val resources: Resources,
) {
    val currentDestination: NavDestination?
        @Composable get() = navController.currentBackStackEntryAsState().value?.destination

    val currentTopLevelDestination: TopLevelDestination?
        @Composable get() = when (currentDestination?.route?.substringAfterLast(".")) {
            "Adoption" -> TopLevelDestination.Adoption
            "Review" -> TopLevelDestination.Review
            "Chat" -> TopLevelDestination.Chat
            "MyPage" -> TopLevelDestination.MyPage
            else -> null
        }

    val topLevelDestination = listOf(
        TopLevelDestination.Adoption,
        TopLevelDestination.Review,
        TopLevelDestination.Chat,
        TopLevelDestination.MyPage
    )

    fun navigateToTopLevelDestination(topLevelDestination: TopLevelDestination) {
        val topLevelNavOptions = navOptions {
            popUpTo(Adoption) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
        when (topLevelDestination) {
            TopLevelDestination.Adoption -> {
                navController.navigate(Adoption, topLevelNavOptions)
            }

            TopLevelDestination.Chat -> {
                navController.navigate(Chat, topLevelNavOptions)
            }

            TopLevelDestination.MyPage -> {
                navController.navigate(MyPage, topLevelNavOptions)
            }

            TopLevelDestination.Review -> {
                navController.navigate(Review, topLevelNavOptions)
            }
        }
    }

}


@Composable
fun rememberAppState(
    navController: NavHostController = rememberNavController(),
    resources: Resources = resources(),
) = remember(navController) {
    BeMyPetAppState(
        navController, resources
    )
}

@Composable
@ReadOnlyComposable
fun resources(): Resources {
    LocalConfiguration.current
    return LocalContext.current.resources
}