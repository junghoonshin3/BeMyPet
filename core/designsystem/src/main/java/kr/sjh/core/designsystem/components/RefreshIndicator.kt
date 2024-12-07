package kr.sjh.core.designsystem.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import kr.sjh.core.designsystem.R
import kr.sjh.core.designsystem.modifier.centerPullToRefreshIndicator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RefreshIndicator(
    modifier: Modifier = Modifier, state: PullToRefreshState, isRefreshing: Boolean, threshold: Dp
) {
    val context = LocalContext.current

    val imageVector by remember {
        derivedStateOf {
            when {
                state.distanceFraction > 0f && state.distanceFraction <= 0.5f && !isRefreshing -> {
                    ImageVector.vectorResource(
                        res = context.resources, resId = R.drawable.dog_face_svgrepo_com
                    )
                }

                state.distanceFraction <= 1f && state.distanceFraction > 0.5f && !isRefreshing -> {
                    ImageVector.vectorResource(
                        res = context.resources, resId = R.drawable.cat_svgrepo_com
                    )
                }

                state.distanceFraction > 1f && !isRefreshing -> {
                    ImageVector.vectorResource(
                        res = context.resources, resId = R.drawable.panda_face_1_svgrepo_com
                    )
                }

                else -> {
                    ImageVector.vectorResource(
                        res = context.resources, resId = R.drawable.sheep_2_svgrepo_com
                    )
                }
            }
        }
    }
    Box(
        modifier = modifier.centerPullToRefreshIndicator(
            state = state, shape = RectangleShape, threshold = threshold
        ), contentAlignment = Alignment.TopCenter
    ) {
        if (state.distanceFraction == 0f) {
            return@Box
        }

        if (isRefreshing) {
            Text(text = context.resources.getString(R.string.refreshing), fontSize = 25.sp)
        } else {
            Image(imageVector = imageVector, contentDescription = "animal")
        }
    }
}