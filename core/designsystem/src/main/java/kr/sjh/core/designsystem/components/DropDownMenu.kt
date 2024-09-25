package kr.sjh.core.designsystem.components

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuBoxScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> DropDownMenu(
    modifier: Modifier = Modifier,
    list: List<T>,
    expanded: Boolean,
    selectedItem: T,
    onExpandedChange: (Boolean) -> Unit,
    onDismissRequest: () -> Unit,
    selectedText: @Composable ExposedDropdownMenuBoxScope.(T) -> Unit,
    menuItem: @Composable ColumnScope.(T) -> Unit
) {
    ExposedDropdownMenuBox(
        modifier = modifier,
        expanded = expanded,
        onExpandedChange = onExpandedChange
    ) {
        selectedText(selectedItem)
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = onDismissRequest) {
            list.forEach { sido ->
                menuItem(sido)
            }
        }
    }
}