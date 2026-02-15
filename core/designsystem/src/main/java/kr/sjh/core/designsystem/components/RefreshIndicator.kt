package kr.sjh.core.designsystem.components

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.AnimationConstants.DefaultDurationMillis
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshState
import androidx.compose.material3.pulltorefresh.pullToRefreshIndicator
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kr.sjh.core.designsystem.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RefreshIndicator(
    modifier: Modifier = Modifier,
    state: PullToRefreshState,
    isRefreshing: Boolean,
    threshold: Dp = 80.dp
) {

    val resId by remember {
        derivedStateOf {
            when {
                state.distanceFraction > 0f && state.distanceFraction <= 0.5f -> {
                    R.drawable.animal_carnivore_cartoon_3_svgrepo_com
                }

                state.distanceFraction <= 1f && state.distanceFraction > 0.5f -> {
                    R.drawable.animal_carnivore_cartoon_8_svgrepo_com
                }

                else -> {
                    R.drawable.animal_bunny_cartoon_svgrepo_com
                }
            }
        }
    }

    val painter = ImageVector.vectorResource(resId)

    Box(
        modifier = modifier.pullToRefreshIndicator(
            elevation = 0.dp,
            shape = CircleShape, containerColor = Color.LightGray,
            state = state,
            isRefreshing = isRefreshing,
            threshold = threshold
        ), contentAlignment = Alignment.Center
    ) {
        Crossfade(
            targetState = isRefreshing,
            animationSpec = tween(durationMillis = DefaultDurationMillis),
            label = "cross_fade"
        ) { refreshing ->
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                if (refreshing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(30.dp),
                        color = Color(0xFFE97341),
                        strokeWidth = 5.dp
                    )
                } else {
                    Image(imageVector = painter, contentDescription = "animal")
                }
            }

        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun RefreshIndicatorPreview() {
    RefreshIndicator(
        state = rememberPullToRefreshState(), isRefreshing = false, threshold = 80.dp
    )
}