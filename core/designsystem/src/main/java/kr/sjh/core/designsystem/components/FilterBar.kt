package kr.sjh.core.designsystem.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.OverscrollEffect
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.ScrollableDefaults
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.overscroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import kr.sjh.core.designsystem.R
import kr.sjh.core.model.FilterType

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun <T : FilterType> FilterBar(
    modifier: Modifier = Modifier, items: List<T>, onFilterType: (T) -> Unit, showFilter: () -> Unit
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        LazyRow(
            modifier = Modifier
                .weight(1f),
            contentPadding = PaddingValues(5.dp),
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items(items) { item ->
                FilterItem(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .border(1.dp, Color.LightGray, RoundedCornerShape(20.dp)),
                    item = item,
                    onFilterType = onFilterType
                )
            }
        }
        Icon(modifier = Modifier
            .clip(CircleShape)
            .clickable {
                showFilter()
            }
            .padding(5.dp)
            .size(30.dp),
            imageVector = ImageVector.vectorResource(id = R.drawable.filter_circle_svgrepo_com),
            contentDescription = "showFilter")
    }
}

@Composable
fun <T : FilterType> FilterItem(
    modifier: Modifier = Modifier, item: T, onFilterType: (T) -> Unit
) {
    Box(modifier = modifier
        .clickable { onFilterType(item) }
        .padding(5.dp)
        .sizeIn(minWidth = 50.dp, minHeight = 30.dp), contentAlignment = Alignment.Center) {
        Text(text = item.filterName)
    }
}