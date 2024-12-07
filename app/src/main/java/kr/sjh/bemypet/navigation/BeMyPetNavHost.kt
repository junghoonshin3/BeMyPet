package kr.sjh.bemypet.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import androidx.navigation.navOptions
import androidx.navigation.navigation
import androidx.navigation.toRoute
import kotlinx.serialization.Serializable
import kr.sjh.bemypet.BeMyPetAppState
import kr.sjh.core.model.adoption.Pet
import kr.sjh.feature.adoption.navigation.Adoption
import kr.sjh.feature.adoption.screen.AdoptionRoute
import kr.sjh.feature.adoption_detail.screen.PetDetailRoute
import kr.sjh.feature.adoption_detail.PetPinedZoomRoute
import kr.sjh.feature.adoption_detail.navigation.PetDetail
import kr.sjh.feature.adoption_detail.navigation.PinchZoom
import kr.sjh.feature.adoption_detail.navigation.navigateToPetDetail
import kr.sjh.feature.adoption_detail.navigation.navigateToPinchZoom
import kr.sjh.feature.favourite.navigation.Favourite
import kr.sjh.feature.favourite.screen.FavouriteRoute
import kr.sjh.feature.mypage.navigation.MyPage
import kr.sjh.feature.mypage.screen.MyPageRoute
import kotlin.reflect.typeOf

@Serializable
data object LoginGraph

@Composable
fun BeMyPetNavHost(
    modifier: Modifier = Modifier, appState: BeMyPetAppState
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
                navController.navigateToPetDetail(PetDetail(petInfo = pet))
            })
        }

        composable<PetDetail>(
            typeMap = mapOf(
                typeOf<Pet>() to CustomNavType.petType,
            )
        ) { backStackEntry ->
            PetDetailRoute(onBack = {
                navController.navigateUp()
            }, navigateToPinchZoom = { pinchZoom ->
                navController.navigateToPinchZoom(pinchZoom)
            })
        }

        dialog<PinchZoom>(
            dialogProperties = DialogProperties(
                usePlatformDefaultWidth = false,
                decorFitsSystemWindows = false,
            )
        ) { backStackEntry ->
            val pinchZoom: PinchZoom = backStackEntry.toRoute()
            PetPinedZoomRoute(pinchZoom, close = {
                navController.navigateUp()
            })
        }

        composable<Favourite> {
            FavouriteRoute(navigateToPetDetail = { pet ->
                navController.navigateToPetDetail(PetDetail(petInfo = pet))
            })
        }

        composable<MyPage> {
            MyPageRoute()
        }


    }
}
