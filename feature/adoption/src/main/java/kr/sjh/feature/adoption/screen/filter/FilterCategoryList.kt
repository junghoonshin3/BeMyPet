package kr.sjh.feature.adoption.screen.filter

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kr.sjh.core.designsystem.R
import kr.sjh.core.designsystem.components.RoundedCornerButton
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
            .padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        item {
            IconButton(modifier = Modifier.padding(start = 5.dp, end = 5.dp), onClick = {
                onFilterEvent(FilterEvent.Reset)
            }) {
                Icon(
                    imageVector = ImageVector.vectorResource(id = R.drawable.refresh_svgrepo_com),
                    contentDescription = "reset"
                )
            }
        }
        items(categories) { category ->
            RoundedCornerButton(modifier = Modifier.padding(5.dp),
                title = if (category.isSelected.value) {
                    category.selectedText.value
                } else {
                    category.type.title
                },
                selected = category.isSelected.value,
                onClick = {
                    onFilterEvent(FilterEvent.SelectedCategory(category))
                })
        }
    }
}