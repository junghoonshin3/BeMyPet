package kr.sjh.core.login.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kr.sjh.core.designsystem.R
import kr.sjh.core.login.state.LoginEvent
import kr.sjh.core.login.state.NavigationState


@Composable
internal fun LoginRoute(
    navigateToMain: () -> Unit,
    navigateToLoginRegister: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val navigationState by viewModel.navigationState.collectAsStateWithLifecycle(NavigationState.None)

    LaunchedEffect(navigationState) {
        when (navigationState) {
            NavigationState.NavigateToMain -> {
                navigateToMain()
            }

            NavigationState.NavigateToLoginRegister -> {
                navigateToLoginRegister()
            }

            NavigationState.None -> {

            }
        }
    }

    LoginScreen(
        onEvent = viewModel::onEvent
    )
}

@Composable
private fun LoginScreen(
    onEvent: (LoginEvent) -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        val context = LocalContext.current
        Image(
            modifier = Modifier.clickable {
                onEvent(LoginEvent.OnGoogleClicked(context))
            },
            imageVector = ImageVector.vectorResource(id = R.drawable.android_neutral_rd_ctn),
            contentDescription = "google_login"
        )
    }
}

