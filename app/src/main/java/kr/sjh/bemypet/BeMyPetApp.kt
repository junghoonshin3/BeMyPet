package kr.sjh.bemypet

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kr.sjh.bemypet.navigation.BeMyPetBottomNavigation
import kr.sjh.bemypet.navigation.BeMyPetNavHost
import kr.sjh.setting.screen.SettingViewModel

@Composable
fun BeMyPetApp(
    appState: BeMyPetAppState = rememberAppState(),
    settingViewModel: SettingViewModel
) {
    Scaffold(snackbarHost = {
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
            appState = appState, modifier = Modifier
                .padding(contentPadding)
                .fillMaxSize(),
            settingViewModel = settingViewModel
        )
    }
}