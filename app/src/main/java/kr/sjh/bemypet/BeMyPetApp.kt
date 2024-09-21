package kr.sjh.bemypet

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kr.sjh.bemypet.navigation.BeMyPetBottomNavigation
import kr.sjh.bemypet.navigation.BeMyPetNavHost
import kr.sjh.bemypet.navigation.LoginGraph
import kr.sjh.feature.adoption.navigation.Adoption

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun BeMyPetApp(
    accountState: AccountState, appState: BeMyPetAppState = rememberAppState()
) {
    val startDestination: Any = when (accountState) {
        AccountState.UserAlreadySignIn -> Adoption
        else -> LoginGraph
    }

    Surface {
        Scaffold(modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
            topBar = {
                TopAppBar(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    currentTopLevelDestination = appState.currentTopLevelDestination,
                )
            }, bottomBar = {
                BeMyPetBottomNavigation(
                    modifier = Modifier.fillMaxWidth(),
                    destinations = appState.topLevelDestination,
                    currentTopLevelDestination = appState.currentTopLevelDestination,
                    currentDestination = appState.currentDestination,
                    navigateToTopLevelDestination = appState::navigateToTopLevelDestination
                )
            }) {
            BeMyPetNavHost(
                startDestination = startDestination,
                appState = appState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(it)
            )
        }
    }
}