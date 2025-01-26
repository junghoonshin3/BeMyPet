package kr.sjh.bemypet

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.common.collect.ImmutableList
import kr.sjh.bemypet.navigation.BeMyPetBottomNavigation
import kr.sjh.bemypet.navigation.BeMyPetNavHost
import kr.sjh.bemypet.navigation.BottomNavItem
import okhttp3.internal.toImmutableList

@Composable
fun BeMyPetApp(
    appState: BeMyPetAppState = rememberAppState(), onChangeDarkTheme: (Boolean) -> Unit
) {
    Scaffold(modifier = Modifier.fillMaxSize(), snackbarHost = {
        SnackbarHost(hostState = appState.snackBarHostState,
            modifier = Modifier.padding(4.dp),
            snackbar = { snackBarData ->
                Snackbar(snackBarData)
            })
    }, bottomBar = {
        BeMyPetBottomNavigation(
            modifier = Modifier.fillMaxWidth(),
            destinations = appState.bottomNavItems,
            currentBottomNavItem = appState.currentBottomNavItem,
            currentDestination = appState.currentDestination,
            navigateToTopLevelDestination = appState::navigateToBottomNavItem
        )
    }) { contentPadding ->
        BeMyPetNavHost(
            appState = appState,
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding),
            onChangeDarkTheme = onChangeDarkTheme
        )
    }
}