package kr.sjh.core.designsystem.components

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import coil.compose.AsyncImage
import coil.request.ImageRequest

@Composable
fun PinchZoomComponent(imageRequest: ImageRequest, onTap: () -> Unit) {
    // 확대 축소 비율
    var scale by remember {
        mutableStateOf(1f)
    }

    // 현재 이미지 위치
    var offset by remember {
        mutableStateOf(Offset.Zero)
    }

    BoxWithConstraints(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        val state = rememberTransformableState { zoomChange, panChange, _ ->
            // 이미지의 확대/축소 비율을 나타냅니다.
            scale = (scale * zoomChange).coerceIn(1f, 5f)

            // 이미지 확대/축소 했을때 확대한 너비를 제외한 나머지 너비
            val extraWidth = (scale - 1) * constraints.maxWidth

            // 이미지 확대/축소 했을때  확대한 높이 제외한 나머지 높이
            val extraHeight = (scale - 1) * constraints.maxHeight

            // 이동 할 수 있는 최대 너비
            val maxX = extraWidth / 2

            // 이동 할 수 있는 최대 높이
            val maxY = extraHeight / 2

            // panChange은 이동량을 Offset 객체로 알려준다.
            // 현재 Offset + 이동 Offset을 하면 이미지가 이동한다.
            // But, 실제 이동량은 Scale을 곱한 만큼 이동하므로 반드시 곱해줘야한다. (하지않으면 느리게 이동)
            offset = Offset(
                x = (offset.x + scale * panChange.x).coerceIn(-maxX, maxX),
                y = (offset.y + scale * panChange.y).coerceIn(-maxY, maxY)
            )
        }
        AsyncImage(modifier = Modifier
            .aspectRatio(constraints.maxWidth.toFloat() / constraints.maxHeight.toFloat())
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                translationX = offset.x
                translationY = offset.y
            }
            .pointerInput(Unit) {
                // 더블 탭 시 원본 이미지 or 2배 확대
                detectTapGestures(onDoubleTap = {
                    scale = if (scale == 1f) {
                        //확대 2배
                        2f
                    } else {
                        //원본
                        1f
                    }
                    offset = if (scale == 1f) {
                        Offset.Zero
                    } else offset
                }, onTap = {
                    onTap()
                })
            }
            .transformable(state),
            model = imageRequest,
            contentDescription = "pinchZoomImage")
    }
}