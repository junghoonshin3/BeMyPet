package kr.sjh.feature.splash.screen

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.ui.platform.LocalContext
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import dagger.hilt.android.AndroidEntryPoint

@SuppressLint("CustomSplashScreen")
@AndroidEntryPoint
class SplashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val activity = LocalContext.current as SplashActivity
            SplashRoute(navigateToMain = {

            }, navigateToLogin = {
                Log.d("navigateToLogin", "navigateToLogin")
                activity.startActivity(
                    Intent(Intent.ACTION_VIEW, Uri.parse("bemypet://login"))
                )
                activity.finish()
            })
        }
    }
}