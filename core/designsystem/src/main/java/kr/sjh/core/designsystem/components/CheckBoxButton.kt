package kr.sjh.core.designsystem.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun CheckBoxButton(
    modifier: Modifier = Modifier,
    title: String,
    selected: Boolean,
    showBorder: Boolean = true,
    onClick: () -> Unit
) {
    SelectableListItem(
        title = title,
        modifier = modifier,
        selected = selected,
        showCheckIcon = true,
        showBorder = showBorder,
        onClick = onClick
    )
}
