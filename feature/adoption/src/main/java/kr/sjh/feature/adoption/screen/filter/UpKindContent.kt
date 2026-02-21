package kr.sjh.feature.adoption.screen.filter

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kr.sjh.core.designsystem.components.CheckBoxButton
import kr.sjh.feature.adoption.state.FilterEvent
import kr.sjh.feature.adoption.state.UpKind

@Composable
fun UpKindContent(
    title: String,
    selectedUpKind: UpKind,
    confirm: (UpKind) -> Unit,
    close: () -> Unit,
) {
    var newUpKind by remember {
        mutableStateOf(selectedUpKind)
    }
    Column(modifier = Modifier.fillMaxWidth()) {
        FilterTopBar(title = title, close = close, confirm = { confirm(newUpKind) })
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(5.dp)
        ) {
            items(UpKind.entries.toTypedArray()) { item ->
                CheckBoxButton(modifier = Modifier.fillMaxWidth(),
                    title = item.title,
                    selected = newUpKind == item,
                    showBorder = false,
                    onClick = {
                        newUpKind = item
                    })
            }
        }
    }
}
