package kr.sjh.core.designsystem.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun Title(modifier: Modifier = Modifier, title: String, fontSize: TextUnit = 25.sp) {
    Box(modifier = modifier, contentAlignment = Alignment.CenterStart) {
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = title,
            fontSize = fontSize,
            fontWeight = FontWeight.Bold
        )
    }
}