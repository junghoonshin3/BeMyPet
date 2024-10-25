package kr.sjh.core.designsystem.modifier

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex


// 이미지와 텍스트를 중앙으로 이동시키기 위한 Modifier
@OptIn(ExperimentalMaterial3Api::class)
fun Modifier.centerPullToRefreshIndicator(
    state: PullToRefreshState,
    threshold: Dp = PullToRefreshDefaults.PositionalThreshold,
    shape: Shape = PullToRefreshDefaults.shape,
    containerColor: Color = Color.Unspecified,
): Modifier = this
    .size(40.dp)
    .drawWithContent {
        clipRect(
            top = 0f, left = -Float.MAX_VALUE, right = Float.MAX_VALUE, bottom = Float.MAX_VALUE
        ) {
            this@drawWithContent.drawContent()
        }
    }
    .graphicsLayer {
        translationY = (state.distanceFraction * threshold.roundToPx()) / 2 - size.center.y
        this.shape = shape
        clip = true
    }
    .background(color = containerColor, shape = shape)
    .zIndex(-1f)

@Composable
fun Modifier.clickableNoRipple(onClick: () -> Unit): Modifier {
    val mutableInteractionSource = remember { MutableInteractionSource() }
    return this.clickable(
        interactionSource = mutableInteractionSource, indication = null, onClick = onClick
    )
}

