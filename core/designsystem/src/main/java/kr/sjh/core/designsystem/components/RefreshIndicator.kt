package kr.sjh.core.designsystem.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kr.sjh.core.designsystem.R
import kr.sjh.core.designsystem.modifier.centerPullToRefreshIndicator

@Composable
fun RefreshIndicator(
    modifier: Modifier = Modifier,
    distanceFraction: () -> Float,
    isRefreshing: Boolean,
    threshold: Dp = 80.dp
) {
    val context = LocalContext.current

    val df by rememberUpdatedState(distanceFraction())

    val imageVector by remember {
        derivedStateOf {
            when {
                df > 0f && df <= 0.5f -> {
                    ImageVector.vectorResource(
                        res = context.resources,
                        resId = R.drawable.animal_carnivore_cartoon_3_svgrepo_com
                    )
                }

                df <= 1f && df > 0.5f -> {
                    ImageVector.vectorResource(
                        res = context.resources,
                        resId = R.drawable.animal_carnivore_cartoon_8_svgrepo_com
                    )
                }

                df > 1f -> {
                    ImageVector.vectorResource(
                        res = context.resources, resId = R.drawable.animal_bunny_cartoon_svgrepo_com
                    )
                }

                else -> {
                    ImageVector.vectorResource(
                        res = context.resources, resId = R.drawable.animal_bear_cartoon_svgrepo_com
                    )
                }
            }
        }
    }
    Box(
        modifier = modifier, contentAlignment = Alignment.TopCenter
    ) {
        if (df == 0f) {
            return@Box
        }

        if (isRefreshing) {
            Text(text = context.resources.getString(R.string.refreshing), fontSize = 25.sp)
        } else {
            Image(imageVector = imageVector, contentDescription = "animal")
        }
    }
}