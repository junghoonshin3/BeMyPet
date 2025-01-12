package kr.sjh.bemypet.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import kr.sjh.bemypet.BeMyPetAppState
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
    val navController = appState.navController

    NavHost(
        modifier = modifier, navController = navController, startDestination = Adoption,
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None },
        popEnterTransition = { EnterTransition.None },
        popExitTransition = { ExitTransition.None },
    ) {
        composable<Adoption> {
            AdoptionRoute(navigateToPetDetail = { pet ->
                navController.navigateToPetDetail(pet)
            })
        }

        composable<PetDetail>(
            typeMap = PetDetail.typeMap
        ) {
            PetDetailRoute(onBack = {
                navController.navigateUp()
            })
        }

        composable<Favourite> {
            FavouriteRoute(navigateToPetDetail = { pet ->
                navController.navigateToPetDetail(pet)
            })
        }
        composable<Setting> {
            SettingRoute(onChangeDarkTheme = onChangeDarkTheme)
        }
    }
}
