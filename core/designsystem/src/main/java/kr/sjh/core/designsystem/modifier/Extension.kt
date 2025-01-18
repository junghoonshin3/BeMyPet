package kr.sjh.core.designsystem.modifier

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier

@Composable
fun Modifier.clickableNoRipple(onClick: () -> Unit): Modifier {
    val mutableInteractionSource = remember { MutableInteractionSource() }
    return this.clickable(
        interactionSource = mutableInteractionSource, indication = null, onClick = onClick
    )
}

