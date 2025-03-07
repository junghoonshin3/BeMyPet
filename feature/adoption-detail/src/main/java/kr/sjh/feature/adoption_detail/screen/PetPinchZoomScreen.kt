package kr.sjh.feature.adoption_detail.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.zIndex
import coil.request.ImageRequest
import kr.sjh.core.designsystem.R
import kr.sjh.core.designsystem.components.BeMyPetTopAppBar
import kr.sjh.core.designsystem.components.PinchZoomComponent

@Composable
fun PetPinedZoomRoute(imageRequest: ImageRequest, close: () -> Unit) {
    PetPinedZoomScreen(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.secondaryContainer),
        close = close,
        imageRequest = imageRequest
    )
}

@Composable
private fun PetPinedZoomScreen(
    modifier: Modifier = Modifier, close: () -> Unit, imageRequest: ImageRequest
) {
    var topBarShow by remember {
        mutableStateOf(true)
    }

    Box(
        modifier = modifier
    ) {
        AnimatedVisibility(
            modifier = Modifier
                .background(Color.Transparent)
                .zIndex(1f),
            visible = topBarShow,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            BeMyPetTopAppBar(
                title = {
                    IconButton(onClick = close) {
                        Icon(
                            imageVector = ImageVector.vectorResource(id = R.drawable.baseline_arrow_back_24),
                            contentDescription = "back"
                        )
                    }
                    Text("사진", style = MaterialTheme.typography.headlineSmall)
                },
                modifier = Modifier.background(MaterialTheme.colorScheme.primaryContainer.copy(0.8f))
            )
        }

        PinchZoomComponent(modifier = Modifier.fillMaxSize(), imageRequest = imageRequest, onTap = {
            topBarShow = !topBarShow
        })


    }
}