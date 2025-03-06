package kr.sjh.bemypet.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import kr.sjh.bemypet.BeMyPetAppState
import kr.sjh.bemypet.canGoBack
import kr.sjh.feature.adoption.navigation.Adoption
import kr.sjh.feature.adoption.screen.AdoptionRoute
import kr.sjh.feature.adoption_detail.navigation.PetDetail
import kr.sjh.feature.adoption_detail.navigation.navigateToPetDetail
import kr.sjh.feature.adoption_detail.screen.PetDetailRoute
import kr.sjh.feature.favourite.navigation.Favourite
import kr.sjh.feature.favourite.screen.FavouriteRoute
import kr.sjh.setting.navigation.Setting
import kr.sjh.setting.screen.SettingRoute

@Composable
fun BeMyPetNavHost(
    modifier: Modifier = Modifier, appState: BeMyPetAppState, onChangeDarkTheme: (Boolean) -> Unit
) {
    NavHost(
        modifier = modifier,
        navController = appState.navController, startDestination = Adoption,
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None },
        popEnterTransition = { EnterTransition.None },
        popExitTransition = { ExitTransition.None },
    ) {
        navigation<Adoption>(
            startDestination = Adoption
        ) {

        }
        composable<Adoption> {
            AdoptionRoute(
                navigateToPetDetail = { pet ->
                    appState.navController.navigateToPetDetail(pet)
                })
        }

        composable<PetDetail>(
            typeMap = PetDetail.typeMap
        ) {
            PetDetailRoute(onBack = {
                if (appState.navController.canGoBack()) {
                    appState.navController.popBackStack()
                }

            })
        }

        composable<Favourite> {
            FavouriteRoute(navigateToPetDetail = { pet ->
                appState.navController.navigateToPetDetail(pet)
            })
        }
        composable<Setting> {
            SettingRoute(onChangeDarkTheme = onChangeDarkTheme)
        }
    }
}
