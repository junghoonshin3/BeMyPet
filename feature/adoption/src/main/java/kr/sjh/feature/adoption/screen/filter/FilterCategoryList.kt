package kr.sjh.feature.adoption.screen.filter

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kr.sjh.core.designsystem.components.RoundedCornerButton
import kr.sjh.core.designsystem.components.SelectableListItem
import kr.sjh.feature.adoption.state.Category
import kr.sjh.feature.adoption.state.FilterEvent

@Composable
fun FilterCategoryList(
    categories: List<Category>, height: Dp, onFilterEvent: (FilterEvent) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SelectableListItem(
            title = "초기화",
            modifier = Modifier.height(44.dp),
            selected = false,
            showCheckIcon = false,
            fillWidth = false,
            onClick = { onFilterEvent(FilterEvent.Reset) }
        )

        Spacer(modifier = Modifier.width(8.dp))

        LazyRow(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items(categories) { category ->
                RoundedCornerButton(
                    modifier = Modifier.height(44.dp),
                    title = if (category.isSelected) {
                        category.selectedText
                    } else {
                        category.type.title
                    },
                    selected = category.isSelected,
                    fillWidth = false,
                    onClick = {
                        onFilterEvent(FilterEvent.SelectedCategory(category))
                    })
            }
        }
    }
}
