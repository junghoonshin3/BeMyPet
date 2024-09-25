package kr.sjh.core.designsystem.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun RoundedCornerButton(modifier: Modifier = Modifier, title: String, onClick: (String) -> Unit) {
    Box(modifier = Modifier
        .clip(RoundedCornerShape(10.dp))
        .border(1.dp, color = Color.LightGray, shape = RoundedCornerShape(10.dp))
        .clickable { onClick(title) }
        .then(modifier), contentAlignment = Alignment.Center) {
        Text(text = title)
    }
}