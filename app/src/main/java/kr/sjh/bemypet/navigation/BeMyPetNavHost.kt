package kr.sjh.bemypet.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import kotlinx.serialization.Serializable
import kr.sjh.bemypet.BeMyPetAppState
import kr.sjh.core.login.navigation.Login
import kr.sjh.core.login.navigation.loginScreen
import kr.sjh.core.login.navigation.navigateToLogin
import kr.sjh.feature.adoption.navigation.Adoption
import kr.sjh.feature.adoption.navigation.navigateToAdoption
import kr.sjh.feature.adoption.screen.AdoptionRoute
import kr.sjh.feature.chat.navigation.Chat
import kr.sjh.feature.mypage.navigation.MyPage
import kr.sjh.feature.mypage.screen.MyPageRoute
import kr.sjh.feature.review.navigation.Review

@Serializable
data object LoginGraph

@Composable
fun BeMyPetNavHost(
    modifier: Modifier = Modifier, appState: BeMyPetAppState, startDestination: Any = LoginGraph
) {

    val navController = appState.navController
    NavHost(
        modifier = modifier, navController = navController, startDestination = startDestination,
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None },
        popEnterTransition = { EnterTransition.None },
        popExitTransition = { ExitTransition.None },
    ) {
        nestedLoginGraph(navigateToLoginRegister = {}, navigateToMain = {
            navController.navigateToAdoption()
        })

        composable<Adoption> {
            AdoptionRoute()
        }

        composable<Review> {
            Box(modifier = Modifier.fillMaxSize()) {
                Text(text = "리뷰 리스트 ^^")
            }
        }

        composable<Chat> {
            Box(modifier = Modifier.fillMaxSize()) {
                Text(text = "채팅방 리스트 ^^")
            }
        }
        composable<MyPage> {
            MyPageRoute(navigateToLogin = navController::navigateToLogin)
        }
    }
}

fun NavGraphBuilder.nestedLoginGraph(
    navigateToMain: () -> Unit, navigateToLoginRegister: () -> Unit
) {
    navigation<LoginGraph>(startDestination = Login) {
        loginScreen(navigateToLoginRegister, navigateToMain)
    }
}