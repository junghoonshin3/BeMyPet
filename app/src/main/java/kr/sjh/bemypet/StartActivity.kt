package kr.sjh.bemypet

import android.os.Build.VERSION
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
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

    private val startViewModel: StartViewModel by viewModels()
    private var isThemeLoaded by mutableStateOf(false)
    private var isTheme by mutableStateOf(false)
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()

        // SplashScreen 유지하면서 테마 값 로드
        splashScreen.setKeepOnScreenCondition { !isThemeLoaded }

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                startViewModel.isDarkTheme.collect { isDark ->
                    isThemeLoaded = true
                    isTheme = isDark
                }
            }
        }

        setContent {
            BeMyPetTheme(isTheme) {
                BeMyPetApp(onChangeDarkTheme = {
                    startViewModel.updateIsDarkTheme(it)
                })
            }
        }
    }
}