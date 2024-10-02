package kr.sjh.core.designsystem.components

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuBoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> DropDownMenu(
    modifier: Modifier = Modifier,
    list: List<T>,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onDismissRequest: () -> Unit,
    selectedText: @Composable ExposedDropdownMenuBoxScope.() -> Unit,
    itemContent: @Composable (T) -> Unit,
) {

    ExposedDropdownMenuBox(
        modifier = modifier,
        expanded = expanded,
        onExpandedChange = onExpandedChange
    ) {
        selectedText()
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = onDismissRequest) {
            list.forEach { sido ->
                itemContent(sido)
            }
        }
    }
}