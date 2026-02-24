package kr.sjh.feature.adoption_detail.screen

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.zIndex
import androidx.core.view.WindowCompat
import coil.request.ImageRequest
import kr.sjh.core.designsystem.components.BeMyPetBackAppBar
import kr.sjh.core.designsystem.components.PinchZoomComponent

@Composable
fun PetPinedZoomRoute(imageRequest: ImageRequest, close: () -> Unit) {
    val view = LocalView.current
    val colorScheme = MaterialTheme.colorScheme

    DisposableEffect(view, colorScheme) {
        val activity = view.context as? Activity
        if (activity == null) {
            onDispose { }
        } else {
            val window = activity.window
            val controller = WindowCompat.getInsetsController(window, view)
            val originalStatusBarColor = window.statusBarColor
            val originalNavigationBarColor = window.navigationBarColor
            val originalLightStatusBars = controller.isAppearanceLightStatusBars
            val originalLightNavigationBars = controller.isAppearanceLightNavigationBars

            window.statusBarColor = colorScheme.primary.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            controller.isAppearanceLightStatusBars = colorScheme.primary.luminance() > 0.5f
            controller.isAppearanceLightNavigationBars = colorScheme.background.luminance() > 0.5f

            onDispose {
                window.statusBarColor = originalStatusBarColor
                window.navigationBarColor = originalNavigationBarColor
                controller.isAppearanceLightStatusBars = originalLightStatusBars
                controller.isAppearanceLightNavigationBars = originalLightNavigationBars
            }
        }
    }

    PetPinedZoomScreen(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        close = close,
        imageRequest = imageRequest
    )
}

@Composable
private fun PetPinedZoomScreen(
    modifier: Modifier = Modifier,
    close: () -> Unit,
    imageRequest: ImageRequest
) {
    var topBarShow by remember { mutableStateOf(true) }

    Box(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .zIndex(2f)
        ) {
            AnimatedVisibility(
                visible = topBarShow,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                BeMyPetBackAppBar(
                    modifier = Modifier.fillMaxWidth(),
                    title = "사진 확대",
                    onBack = close,
                    roundedBottom = false,
                )
            }
        }

        PinchZoomComponent(
            modifier = Modifier.fillMaxSize(),
            imageRequest = imageRequest,
            onTap = { topBarShow = !topBarShow }
        )
    }
}
