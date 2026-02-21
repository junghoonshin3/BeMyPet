package kr.sjh.feature.adoption.screen.filter

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kr.sjh.core.designsystem.R
import kr.sjh.core.designsystem.theme.RoundedCorner12

@Composable
fun FilterTopBar(title: String, close: () -> Unit, confirm: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .padding(horizontal = 8.dp)
        ) {
            IconButton(
                modifier = Modifier
                    .size(48.dp)
                    .align(Alignment.CenterStart)
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCorner12),
                onClick = close
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.close_svgrepo_com),
                    contentDescription = "닫기",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 56.dp),
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            IconButton(
                modifier = Modifier
                    .size(48.dp)
                    .align(Alignment.CenterEnd)
                    .background(MaterialTheme.colorScheme.secondaryContainer, RoundedCorner12),
                onClick = confirm
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.check_svgrepo_com__1_),
                    contentDescription = "적용",
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }
}
