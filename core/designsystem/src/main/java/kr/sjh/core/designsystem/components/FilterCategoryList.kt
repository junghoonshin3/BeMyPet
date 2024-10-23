package kr.sjh.core.designsystem.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import kr.sjh.core.designsystem.R
import kr.sjh.core.model.FilterCategory

@Composable
fun <T : FilterCategory> FilterCategoryList(
    modifier: Modifier = Modifier,
    items: List<T>,
    contentPadding: PaddingValues = PaddingValues(5.dp),
    onShow: () -> Unit,
    itemContent: @Composable (T) -> Unit,
) {

    Row(
        modifier = modifier,

        verticalAlignment = Alignment.CenterVertically
    ) {
        LazyRow(
            modifier = Modifier.weight(1f),
            contentPadding = contentPadding,
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items(items) { item ->
                itemContent(item)
            }
        }
        Icon(modifier = Modifier
            .clip(CircleShape)
            .clickable {
                onShow()
            }
            .padding(5.dp)
            .size(30.dp),
            imageVector = ImageVector.vectorResource(id = R.drawable.filter_circle_svgrepo_com),
            contentDescription = "showFilter")
    }
}