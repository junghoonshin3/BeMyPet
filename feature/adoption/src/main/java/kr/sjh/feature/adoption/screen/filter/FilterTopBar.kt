package kr.sjh.feature.adoption.screen.filter

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kr.sjh.core.designsystem.R

@Composable
fun FilterTopBar(title: String, close: () -> Unit, confirm: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .drawBehind {
                val stokeWidth = 1.dp.toPx()
                val y = size.height - stokeWidth / 2
                drawLine(
                    Color.LightGray,
                    Offset(0f, y),
                    Offset(size.width, y),
                    stokeWidth
                )
            }
            .padding(5.dp)

    ) {
        IconButton(
            modifier = Modifier.align(Alignment.CenterEnd), onClick = confirm
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(R.drawable.check_svgrepo_com__1_),
                contentDescription = ""
            )
        }
        Text(
            modifier = Modifier.align(Alignment.Center),
            text = title,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp
        )
        IconButton(modifier = Modifier.align(Alignment.CenterStart), onClick = close) {
            Icon(
                imageVector = ImageVector.vectorResource(R.drawable.close_svgrepo_com),
                contentDescription = ""
            )
        }
    }
}