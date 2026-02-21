package kr.sjh.core.designsystem.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun RoundedCornerButton(
    modifier: Modifier = Modifier,
    title: String,
    onClick: () -> Unit,
    selected: Boolean = false,
    showBorder: Boolean = true,
    fillWidth: Boolean = true,
) {
    SelectableListItem(
        title = title,
        modifier = modifier,
        selected = selected,
        showCheckIcon = false,
        showBorder = showBorder,
        fillWidth = fillWidth,
        onClick = onClick
    )
}
