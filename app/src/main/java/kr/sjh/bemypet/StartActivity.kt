package kr.sjh.bemypet

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
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
    private var hasCheckedNotificationPermission = false

    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            Log.d("StartActivity", "POST_NOTIFICATIONS granted=$granted")
        }

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
                startViewModel.session.collect { session ->
                    val pushSyncPrefs = getSharedPreferences(PUSH_PREF_NAME, MODE_PRIVATE)
                    if (session is SessionState.Authenticated) {
                        val userId = session.user.id
                        pushSyncPrefs.edit { putString(KEY_CURRENT_USER_ID, userId) }

                        startViewModel.touchLastActive(userId)

                        FirebaseMessaging.getInstance().token
                            .addOnSuccessListener { token ->
                                startViewModel.syncPushSubscription(userId, token)
                            }
                    } else {
                        pushSyncPrefs.edit { remove(KEY_CURRENT_USER_ID) }
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

    override fun onStart() {
        super.onStart()
        ensureNotificationPermissionIfNeeded()
    }

    private fun ensureNotificationPermissionIfNeeded() {
        if (hasCheckedNotificationPermission) {
            Log.d("StartActivity", "skip notification permission: already checked")
            return
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            hasCheckedNotificationPermission = true
            Log.d("StartActivity", "skip notification permission: sdk=${Build.VERSION.SDK_INT}")
            return
        }

        val hasPermission = checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
        if (hasPermission) {
            hasCheckedNotificationPermission = true
            Log.d("StartActivity", "skip notification permission: already granted")
            return
        }

        hasCheckedNotificationPermission = true
        Log.d("StartActivity", "requesting POST_NOTIFICATIONS permission")
        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }
}
