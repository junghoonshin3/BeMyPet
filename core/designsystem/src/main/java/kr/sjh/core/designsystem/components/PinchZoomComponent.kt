package kr.sjh.core.designsystem.components

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
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
import kotlin.math.max

@Composable
fun PinchZoomComponent(
    modifier: Modifier = Modifier,
    imageRequest: ImageRequest,
    onTap: () -> Unit
) {
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    BoxWithConstraints(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        fun clampOffset(current: Offset, currentScale: Float): Offset {
            val extraWidth = (currentScale - 1f) * constraints.maxWidth
            val extraHeight = (currentScale - 1f) * constraints.maxHeight
            val maxX = max(0f, extraWidth / 2f)
            val maxY = max(0f, extraHeight / 2f)
            return Offset(
                x = current.x.coerceIn(-maxX, maxX),
                y = current.y.coerceIn(-maxY, maxY)
            )
        }

        AsyncImage(
            modifier = Modifier
                .aspectRatio(constraints.maxWidth.toFloat() / constraints.maxHeight.toFloat())
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    translationX = offset.x
                    translationY = offset.y
                }
                .pointerInput(Unit) {
                    detectTapGestures(
                        onDoubleTap = { tapOffset ->
                            if (scale == 1f) {
                                val targetScale = 2f
                                val center = Offset(size.width / 2f, size.height / 2f)
                                scale = targetScale
                                offset = clampOffset(
                                    current = (center - tapOffset) * (targetScale - 1f),
                                    currentScale = scale
                                )
                            } else {
                                scale = 1f
                                offset = Offset.Zero
                            }
                        },
                        onTap = { onTap() }
                    )
                }
                .pointerInput(Unit) {
                    detectTransformGestures { centroid, pan, zoom, _ ->
                        val oldScale = scale
                        val newScale = (oldScale * zoom).coerceIn(1f, 5f)
                        val scaleFactor = newScale / oldScale
                        val contentCenter = Offset(size.width / 2f, size.height / 2f)

                        val moved = offset + pan
                        val pinchFocusedOffset =
                            moved + (centroid - contentCenter) * (1f - scaleFactor)

                        scale = newScale
                        offset = clampOffset(
                            current = pinchFocusedOffset,
                            currentScale = scale
                        )
                    }
                },
            model = imageRequest,
            contentDescription = "pinchZoomImage"
        )
    }
}
