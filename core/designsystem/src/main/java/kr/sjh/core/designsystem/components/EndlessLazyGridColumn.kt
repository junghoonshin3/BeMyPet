package kr.sjh.core.designsystem.components

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.rememberLazyListState
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
    items: List<T>,
    columns: GridCells = GridCells.Fixed(3),
    itemKey: (T) -> Any,
    loadMore: () -> Unit,
    itemContent: @Composable (T) -> Unit,
) {
    rememberLazyListState()

    val reachedBottom: Boolean by remember { derivedStateOf { gridState.reachedBottom() } }

    // load more if scrolled to bottom
    LaunchedEffect(reachedBottom) {
        if (reachedBottom) loadMore()
    }

    LazyVerticalGrid(
        modifier = modifier,
        state = gridState,
        columns = columns,
        contentPadding = PaddingValues(5.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        userScrollEnabled = userScrollEnabled
    ) {
        items(items = items, key = { item: T -> itemKey(item) }) { item ->
            itemContent(item)
        }
    }
}

private fun LazyGridState.reachedBottom(buffer: Int = 1): Boolean {
    val lastVisibleItem = this.layoutInfo.visibleItemsInfo.lastOrNull()
    return lastVisibleItem?.index != 0 && lastVisibleItem?.index == this.layoutInfo.totalItemsCount - buffer
}