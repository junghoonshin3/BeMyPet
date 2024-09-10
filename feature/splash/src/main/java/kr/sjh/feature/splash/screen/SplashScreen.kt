package kr.sjh.feature.splash.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kr.sjh.core.designsystem.theme.background_color
import kr.sjh.feature.splash.state.AccountState

@Composable
internal fun SplashRoute(
    navigateToMain: () -> Unit,
    navigateToLogin: () -> Unit,
    viewModel: SplashViewModel = hiltViewModel()
) {

    val accountState by viewModel.accountState.collectAsStateWithLifecycle()

    LaunchedEffect(key1 = accountState) {
        when (accountState) {
            AccountState.Loading -> {

            }

            AccountState.UserAlreadySignIn -> {
                navigateToMain()
            }

            AccountState.UserNotSignIn -> {
                navigateToLogin()
            }
        }
    }

    SplashScreen()
}

@Composable
private fun SplashScreen() {

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = background_color),
        contentAlignment = Alignment.Center
    ) {
        Image(
            modifier = Modifier.size(160.dp),
            painter = painterResource(id = kr.sjh.core.designsystem.R.drawable.dog),
            contentDescription = "icon"
        )
    }
}