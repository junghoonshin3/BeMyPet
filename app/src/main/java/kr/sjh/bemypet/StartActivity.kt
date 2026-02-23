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
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.core.content.edit
import kr.sjh.core.model.SessionState
import kr.sjh.core.designsystem.theme.BeMyPetTheme

private const val PUSH_PREF_NAME = "bemypet_push_sync"
private const val KEY_CURRENT_USER_ID = "current_user_id"

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

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                startViewModel.pushSyncState.collect { pushSyncState ->
                    val pushSyncPrefs = getSharedPreferences(PUSH_PREF_NAME, MODE_PRIVATE)
                    val session = pushSyncState.session
                    if (session is SessionState.Authenticated && pushSyncState.hasSeenOnboarding) {
                        val userId = session.user.id.trim()
                        if (userId.isBlank()) {
                            pushSyncPrefs.edit { remove(KEY_CURRENT_USER_ID) }
                            startViewModel.clearFavoriteInterestSyncUser()
                            return@collect
                        }
                        pushSyncPrefs.edit { putString(KEY_CURRENT_USER_ID, userId) }

                        startViewModel.touchLastActive(userId)
                        startViewModel.syncInterestProfileFromFavoritesOnce(userId)

                        FirebaseMessaging.getInstance().token
                            .addOnSuccessListener { token ->
                                startViewModel.syncPushSubscription(
                                    userId = userId,
                                    token = token,
                                    pushOptIn = pushSyncState.pushOptIn,
                                )
                            }
                    } else {
                        pushSyncPrefs.edit { remove(KEY_CURRENT_USER_ID) }
                        startViewModel.clearFavoriteInterestSyncUser()
                    }
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
                    startViewModel = startViewModel,
                    onChangeDarkTheme = {
                        startViewModel.updateIsDarkTheme(it)
                    })
            }
        }
    }
}
