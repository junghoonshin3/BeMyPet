package kr.sjh.core.login.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import kotlinx.serialization.Serializable
import kr.sjh.core.login.screen.LoginRoute

@Serializable
data object Login

fun NavController.navigateToLogin(
    navOptionsBuilder: NavOptionsBuilder.() -> Unit = {
        launchSingleTop = true
        //TODO 0은 뭐야?
        popUpTo(0) { inclusive = true }
    }
) {
    navigate(Login, builder = navOptionsBuilder)
}

fun NavGraphBuilder.loginScreen(
    navigateToLoginRegister: () -> Unit,
    navigateToMain: () -> Unit
) {
    composable<Login> {
        LoginRoute(
            navigateToLoginRegister = navigateToLoginRegister,
            navigateToMain = navigateToMain
        )
    }
}