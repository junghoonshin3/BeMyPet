package kr.sjh.core.designsystem.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import kr.sjh.core.designsystem.R
import kr.sjh.core.designsystem.theme.DefaultAppBarHeight
import kr.sjh.core.designsystem.theme.RoundedCornerBottom24

@Composable
fun BeMyPetTopAppBar(
    title: @Composable () -> Unit = {},
    content: @Composable () -> Unit = {},
    iconButton: @Composable () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = Modifier
            .heightIn(min = DefaultAppBarHeight)
            .then(modifier)
            .padding(bottom = 4.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.Start
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            title()
            Spacer(modifier = Modifier.weight(1f))
            iconButton()
        }

        content()
    }
}

@Composable
fun BeMyPetBackAppBar(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    containerColor: Color = MaterialTheme.colorScheme.primary,
    titleColor: Color = MaterialTheme.colorScheme.onPrimary,
    roundedBottom: Boolean = true,
    actions: @Composable RowScope.() -> Unit = {},
) {
    val shapeModifier = if (roundedBottom) {
        Modifier
            .background(containerColor, RoundedCornerBottom24)
            .clip(RoundedCornerBottom24)
    } else {
        Modifier.background(containerColor, RoundedCornerShape(0.dp))
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .then(shapeModifier)
            .statusBarsPadding()
            .heightIn(min = DefaultAppBarHeight)
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            modifier = Modifier.size(40.dp),
            onClick = onBack
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(id = R.drawable.baseline_arrow_back_24),
                contentDescription = "back",
                tint = titleColor
            )
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = if (subtitle.isNullOrBlank()) Arrangement.Center else Arrangement.spacedBy(2.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = titleColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (!subtitle.isNullOrBlank()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = titleColor.copy(alpha = 0.9f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End,
            content = actions
        )
    }
}
