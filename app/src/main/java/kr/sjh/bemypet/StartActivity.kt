package kr.sjh.bemypet

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kr.sjh.core.designsystem.theme.BeMyPetTheme

@AndroidEntryPoint
class StartActivity : ComponentActivity() {

    private val viewModel: StartViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splash = installSplashScreen()
        super.onCreate(savedInstanceState)

        var accountState by mutableStateOf(AccountState.Loading)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.accountState.collect {
                    accountState = it
                }
            }
        }

        viewModel.initialize()

        splash.setKeepOnScreenCondition {
            accountState == AccountState.Loading
        }

        enableEdgeToEdge()

        setContent {
            BeMyPetTheme {
                if (accountState != AccountState.Loading) {
                    BeMyPetApp(accountState)
                }
            }
        }
    }
}