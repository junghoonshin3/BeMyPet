package kr.sjh.core.designsystem.components

import android.view.FrameMetrics.ANIMATION_DURATION
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kr.sjh.core.designsystem.R
import kr.sjh.core.model.FilterCategory

@Composable
fun <T : FilterCategory> ExpandableLazyColumn(
    modifier: Modifier = Modifier,
    headerItems: List<T>,
    listState: LazyListState = rememberLazyListState(),
    header: @Composable (T) -> Unit,
) {

    LazyColumn(modifier = modifier, state = listState) {
        headerItems.onEachIndexed { index, item ->
            item {
                header(item)
            }
        }
    }
}

@Composable
fun <T : FilterCategory> SectionHeader(
    category: T, optionContent: @Composable (T) -> Unit
) {
    Column {
        Row(
            modifier = Modifier
                .background(Color.LightGray)
                .padding(vertical = 8.dp, horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                fontSize = 20.sp, text = category.categoryName, modifier = Modifier.weight(1f)
            )
        }
        optionContent(category)
    }
}