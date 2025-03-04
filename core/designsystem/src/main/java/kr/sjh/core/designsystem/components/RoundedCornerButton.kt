package kr.sjh.core.designsystem.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp

@Composable
fun RoundedCornerButton(
    modifier: Modifier = Modifier,
    title: String,
    onClick: () -> Unit,
    selected: Boolean = false,
) {
    Box(modifier = Modifier
        .sizeIn(minWidth = 48.dp, minHeight = 48.dp)
        .border(
            1.dp,
            color = if (selected) Color.Red else Color.LightGray,
            shape = RoundedCornerShape(10.dp)
        )
        .clip(RoundedCornerShape(10.dp))
        .clickable { onClick() }
        .then(modifier), contentAlignment = Alignment.Center) {
        Text(text = title, style = MaterialTheme.typography.bodySmall)
    }
}