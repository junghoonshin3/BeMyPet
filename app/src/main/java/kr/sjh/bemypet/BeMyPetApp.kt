package kr.sjh.bemypet

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kr.sjh.bemypet.navigation.BeMyPetBottomNavigation
import kr.sjh.bemypet.navigation.BeMyPetNavHost

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun BeMyPetApp(
    appState: BeMyPetAppState = rememberAppState()
) {
    Surface(
        contentColor = MaterialTheme.colorScheme.surfaceContainer,
        color = MaterialTheme.colorScheme.surface
    ) {
        Scaffold(modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(), snackbarHost = {
            SnackbarHost(
                hostState = appState.snackBarHostState,
                modifier = Modifier.padding(4.dp),
                snackbar = { snackBarData ->
                    Snackbar(snackBarData)
                }
            )
        },
            bottomBar = {
                BeMyPetBottomNavigation(
                    modifier = Modifier.fillMaxWidth(),
                    destinations = appState.bottomNavItems,
                    currentBottomNavItem = appState.currentBottomNavItem,
                    currentDestination = appState.currentDestination,
                    navigateToTopLevelDestination = appState::navigateToBottomNavItem
                )
            }) {
            BeMyPetNavHost(
                appState = appState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(it)
            )
        }
    }
}