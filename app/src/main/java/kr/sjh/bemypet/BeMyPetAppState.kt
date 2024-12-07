package kr.sjh.bemypet

import android.content.res.Resources
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import kr.sjh.bemypet.navigation.BottomNavItem
import kr.sjh.core.common.snackbar.SnackBarManager
import kr.sjh.feature.adoption.navigation.Adoption
import kr.sjh.feature.favourite.navigation.Favourite
import kr.sjh.feature.mypage.navigation.MyPage

class BeMyPetAppState(
    val navController: NavHostController,
    val resources: Resources,
    val snackBarHostState: SnackbarHostState,
    val snackBarManager: SnackBarManager,
    coroutineScope: CoroutineScope,
) {
    init {
        coroutineScope.launch {
            snackBarManager.snackBarMessages.filterNotNull().collect { msg ->
                val (message, duration) = msg
                snackBarHostState.showSnackbar(
                    message,
                    null,
                    withDismissAction = true,
                    duration = duration ?: SnackbarDuration.Short
                )
                snackBarManager.clean()
            }
        }
    }

    val currentDestination: NavDestination?
        @Composable get() = navController.currentBackStackEntryAsState().value?.destination

    val currentBottomNavItem: BottomNavItem?
        @Composable get() = when (currentDestination?.route?.substringAfterLast(".")) {
            "Adoption" -> BottomNavItem.Adoption
            "Favourite" -> BottomNavItem.Favourite
            "MyPage" -> BottomNavItem.MyPage
            else -> null
        }

    val bottomNavItems = listOf(
        BottomNavItem.Adoption, BottomNavItem.Favourite, BottomNavItem.MyPage
    )

    fun navigateToBottomNavItem(bottomNavItem: BottomNavItem) {
        val topLevelNavOptions = navOptions {
            popUpTo(Adoption) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
        when (bottomNavItem) {
            BottomNavItem.Adoption -> {
                navController.navigate(Adoption, topLevelNavOptions)
            }

            BottomNavItem.MyPage -> {
                navController.navigate(MyPage, topLevelNavOptions)
            }

            BottomNavItem.Favourite -> {
                navController.navigate(Favourite, topLevelNavOptions)
            }
        }
    }
}


@Composable
fun rememberAppState(
    navController: NavHostController = rememberNavController(),
    resources: Resources = resources(),
    snackBarHostState: SnackbarHostState = remember { SnackbarHostState() },
    snackBarManager: SnackBarManager = SnackBarManager,
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
) = remember(navController) {
    BeMyPetAppState(
        navController, resources, snackBarHostState, snackBarManager, coroutineScope
    )
}

@Composable
@ReadOnlyComposable
fun resources(): Resources {
    LocalConfiguration.current
    return LocalContext.current.resources
}