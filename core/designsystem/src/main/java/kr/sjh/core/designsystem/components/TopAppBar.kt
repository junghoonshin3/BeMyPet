package kr.sjh.core.designsystem.components

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TopAppBar(
    modifier: Modifier = Modifier,
    title: String,
    @DrawableRes leftRes: Int? = null,
    @DrawableRes rightRes: Int? = null,
    onLeft: () -> Unit = {},
    onRight: () -> Unit = {}
) {
    Box(modifier = modifier) {
        leftRes?.let { resource ->
            TopBarIcon(
                modifier = Modifier
                    .clip(CircleShape)
                    .align(Alignment.CenterStart)
                    .size(50.dp)
                    .clickable { onLeft() }, iconRes = resource
            )
        }

        Text(
            fontWeight = FontWeight.Bold,
            fontSize = 19.sp,
            text = title,
            modifier = Modifier.align(Alignment.Center)
        )
        rightRes?.let { resource ->
            TopBarIcon(
                modifier = Modifier
                    .clip(CircleShape)
                    .align(Alignment.CenterEnd)
                    .size(50.dp)
                    .clickable { onRight() }, iconRes = resource
            )
        }

    }

}

@Composable
private fun TopBarIcon(modifier: Modifier = Modifier, iconRes: Int) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Icon(
            imageVector = ImageVector.vectorResource(id = iconRes),
            contentDescription = "TopBarIcon"
        )
    }
}