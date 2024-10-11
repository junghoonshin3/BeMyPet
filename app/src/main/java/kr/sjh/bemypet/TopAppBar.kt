package kr.sjh.bemypet

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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kr.sjh.bemypet.navigation.TopNavItem

@Composable
fun TopAppBar(
    modifier: Modifier = Modifier, currentTopNavItem: TopNavItem?,
    onLeft: (() -> Unit)? = null,
    onRight: (() -> Unit)? = null
) {

    if (currentTopNavItem == null) return

    Box(modifier = modifier) {
        currentTopNavItem.leftRes?.let { resource ->
            TopBarIcon(
                modifier = Modifier
                    .clip(CircleShape)
                    .align(Alignment.CenterStart)
                    .size(24.dp)
                    .clickable { onLeft?.invoke() }, iconRes = resource
            )
        }

        Text(
            fontWeight = FontWeight.Bold,
            fontSize = 19.sp,
            text = stringResource(id = currentTopNavItem.title),
            modifier = Modifier.align(Alignment.Center)
        )

        currentTopNavItem.rightRes?.let { resource ->
            TopBarIcon(
                modifier = Modifier
                    .clip(CircleShape)
                    .align(Alignment.CenterEnd)
                    .size(24.dp)
                    .clickable {
                        onRight?.invoke()
                    }, iconRes = resource
            )
        }

//        leftRes?.let { resource ->
//            TopBarIcon(
//                modifier = Modifier
//                    .clip(CircleShape)
//                    .align(Alignment.CenterStart)
//                    .size(50.dp)
//                    .clickable { onRight() }, iconRes = resource
//            )
//        }

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