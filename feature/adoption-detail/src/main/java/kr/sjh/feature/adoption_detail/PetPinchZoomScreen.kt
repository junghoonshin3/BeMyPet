package kr.sjh.feature.adoption_detail

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import coil.request.ImageRequest
import kr.sjh.core.designsystem.R
import kr.sjh.core.designsystem.components.PinchZoomImage
import kr.sjh.core.designsystem.modifier.clickableNoRipple
import kr.sjh.feature.adoption_detail.navigation.PinchZoom

@Composable
fun PetPinedZoomRoute(pinchZoom: PinchZoom, close: () -> Unit) {
    val context = LocalContext.current
    val imageRequest = ImageRequest.Builder(context).data(pinchZoom.imageUrl).build()
    PetPinedZoomScreen(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        close = close,
        imageRequest = imageRequest
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PetPinedZoomScreen(
    modifier: Modifier = Modifier, close: () -> Unit, imageRequest: ImageRequest
) {
    var topBarShow by remember {
        mutableStateOf(true)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
    ) {

        PinchZoomImage(imageRequest = imageRequest, onTap = {
            topBarShow = !topBarShow
        })

        AnimatedVisibility(
            modifier = Modifier
                .background(Color.Transparent)
                .zIndex(1f),
            visible = topBarShow,
            enter = slideInVertically(
                initialOffsetY = { fullHeight -> -fullHeight },
                animationSpec = tween(durationMillis = 150, easing = LinearOutSlowInEasing)
            ),
            exit = slideOutVertically(
                targetOffsetY = { fullHeight -> -fullHeight },
                animationSpec = tween(durationMillis = 250, easing = FastOutLinearInEasing)
            )
        ) {
            TopAppBar(navigationIcon = {
                IconButton(onClick = close) {
                    Icon(
                        imageVector = ImageVector.vectorResource(id = R.drawable.baseline_arrow_back_24),
                        contentDescription = "back"
                    )
                }
            }, title = { Text("사진") }, colors = TopAppBarDefaults.topAppBarColors(
//                containerColor = Color.Transparent,
//                titleContentColor = Color.White,
//                navigationIconContentColor = Color.White
            )
            )
        }
    }
}