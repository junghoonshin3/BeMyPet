package kr.sjh.feature.adoption_detail.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import coil.request.ImageRequest
import kr.sjh.core.designsystem.R
import kr.sjh.core.designsystem.components.BeMyPetTopAppBar
import kr.sjh.core.designsystem.components.PinchZoomComponent
import kr.sjh.core.designsystem.theme.RoundedCornerBottom24

@Composable
fun PetPinedZoomRoute(imageRequest: ImageRequest, close: () -> Unit) {
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
        AnimatedVisibility(
            modifier = Modifier
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
                            contentDescription = "back",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    Text(
                        text = "사진 확대",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp, RoundedCornerBottom24)
                    .background(MaterialTheme.colorScheme.primary, RoundedCornerBottom24)
                    .clip(RoundedCornerBottom24)
            )
        }

        PinchZoomComponent(
            modifier = Modifier.fillMaxSize(),
            imageRequest = imageRequest,
            onTap = { topBarShow = !topBarShow }
        )
    }
}
