package kr.sjh.bemypet

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kr.sjh.bemypet.navigation.BeMyPetBottomNavigation
import kr.sjh.bemypet.navigation.BeMyPetNavHost

@Composable
fun BeMyPetApp(
    startViewModel: StartViewModel,
    appState: BeMyPetAppState = rememberAppState(),
    onChangeDarkTheme: (Boolean) -> Unit
) {
    // SDK 35 이상 타켓팅 시 Statusbar와 AppBar 겹치는 현상 대응
    val modifier = if (Build.VERSION.SDK_INT > Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primary)
            .statusBarsPadding()
    } else {
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primary)
    }

    val session by startViewModel.session.collectAsStateWithLifecycle()
    val hasSeenOnboarding by startViewModel.hasSeenOnboarding.collectAsStateWithLifecycle(
        initialValue = false
    )

    Scaffold(modifier = modifier, snackbarHost = {
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
    }) { p ->
        val bottomPadding = p.calculateBottomPadding()
        BeMyPetNavHost(
            appState = appState,
            session = session,
            hasSeenOnboarding = hasSeenOnboarding,
            modifier = Modifier
                .fillMaxSize(),
            bottomPadding = bottomPadding,
            onChangeDarkTheme = onChangeDarkTheme,
            onCompleteOnboarding = startViewModel::completeOnboarding
        )
    }
}
