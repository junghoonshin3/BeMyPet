package kr.sjh.bemypet

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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

    Surface(modifier = Modifier.fillMaxSize()) {
        Scaffold(bottomBar = {
            Log.d("sjh", "appState : ${appState.currentDestination?.route}")
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