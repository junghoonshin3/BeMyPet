package kr.sjh.bemypet

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.gms.ads.MobileAds
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
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

        val backgroundScope = CoroutineScope(Dispatchers.IO)

        backgroundScope.launch {
            MobileAds.initialize(this@StartActivity) {
                Log.d("sjh", "ads init")
            }
        }

        setContent {
            BeMyPetTheme(isTheme) {
                BeMyPetApp(
                    onChangeDarkTheme = {
                        startViewModel.updateIsDarkTheme(it)
                    })
            }
        }
    }
}