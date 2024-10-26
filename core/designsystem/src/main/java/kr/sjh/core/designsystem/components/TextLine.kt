package kr.sjh.core.designsystem.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Composable
fun TextLine(
    title: String,
    content: String,
    titleTextStyle: TextStyle = TextStyle.Default.copy(fontWeight = FontWeight.Bold),
    contentTextStyle: TextStyle = TextStyle.Default
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(5.dp)
    ) {
        Text(style = titleTextStyle, text = title, modifier = Modifier.fillMaxWidth(0.3f))
        Text(
            style = contentTextStyle,
            modifier = Modifier.fillMaxWidth(0.7f),
            text = content
        )
    }
}
