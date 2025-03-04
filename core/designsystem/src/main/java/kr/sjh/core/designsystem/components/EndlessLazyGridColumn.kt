package kr.sjh.core.designsystem.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun <T> EndlessLazyGridColumn(
    modifier: Modifier = Modifier,
    gridState: LazyGridState = rememberLazyGridState(),
    userScrollEnabled: Boolean,
    contentPadding: PaddingValues = PaddingValues(5.dp),
    isLoadMore: Boolean = false,
    items: List<T>,
    columns: GridCells = GridCells.Fixed(2),
    itemKey: (T) -> Any,
    loadMore: () -> Unit,
    itemContent: @Composable (T) -> Unit,
) {

    val reachedBottom: Boolean by remember { derivedStateOf { gridState.reachedBottom() } }

    LaunchedEffect(reachedBottom) {
        if (reachedBottom) loadMore()
    }

    LazyVerticalGrid(
        modifier = modifier,
        state = gridState,
        columns = columns,
        contentPadding = contentPadding,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        userScrollEnabled = userScrollEnabled
    ) {
        items(items = items, key = { item: T -> itemKey(item) }) { item ->
            itemContent(item)
        }

        item(span = { GridItemSpan(2) }) {
            if (isLoadMore) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                ) {
                    LoadingComponent()
                }
            }
        }
    }
}

private fun LazyGridState.reachedBottom(buffer: Int = 1): Boolean {
    val lastVisibleItem = this.layoutInfo.visibleItemsInfo.lastOrNull()
    return lastVisibleItem?.index != 0 && lastVisibleItem?.index == this.layoutInfo.totalItemsCount - buffer
}