package kr.sjh.feature.adoption.screen.filter

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kr.sjh.core.designsystem.R
import kr.sjh.core.designsystem.components.RoundedCornerButton
import kr.sjh.core.designsystem.theme.RoundedCorner12
import kr.sjh.feature.adoption.state.Category
import kr.sjh.feature.adoption.state.FilterEvent

@Composable
fun FilterCategoryList(
    categories: List<Category>, height: Dp, onFilterEvent: (FilterEvent) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        item {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(MaterialTheme.colorScheme.surface, RoundedCorner12)
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCorner12)
                    .clip(RoundedCorner12)
                    .clickable {
                        onFilterEvent(FilterEvent.Reset)
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(id = R.drawable.refresh_svgrepo_com),
                    contentDescription = "reset",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        items(categories) { category ->
            RoundedCornerButton(
                title = if (category.isSelected) {
                    category.selectedText
                } else {
                    category.type.title
                },
                selected = category.isSelected,
                onClick = {
                    onFilterEvent(FilterEvent.SelectedCategory(category))
                })
        }
    }
}
