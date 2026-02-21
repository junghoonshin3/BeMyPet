package kr.sjh.core.designsystem.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

enum class BeMyPetDialogActionStyle {
    Primary,
    Secondary,
    Destructive
}

@Composable
fun BeMyPetDialogContainer(
    onDismissRequest: () -> Unit,
    title: String,
    modifier: Modifier = Modifier,
    message: String? = null,
    content: (@Composable ColumnScope.() -> Unit)? = null,
    actions: @Composable ColumnScope.() -> Unit
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            modifier = modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            tonalElevation = 0.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
                if (!message.isNullOrBlank()) {
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
                if (content != null) {
                    content()
                }
                actions()
            }
        }
    }
}

@Composable
fun BeMyPetDialogActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    style: BeMyPetDialogActionStyle = BeMyPetDialogActionStyle.Primary
) {
    when (style) {
        BeMyPetDialogActionStyle.Secondary -> {
            OutlinedButton(
                onClick = onClick,
                modifier = modifier.height(44.dp),
                enabled = enabled,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.secondary,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) {
                Text(text = text, style = MaterialTheme.typography.labelLarge)
            }
        }

        BeMyPetDialogActionStyle.Destructive -> {
            Button(
                onClick = onClick,
                modifier = modifier.height(44.dp),
                enabled = enabled,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError,
                    disabledContainerColor = MaterialTheme.colorScheme.errorContainer,
                    disabledContentColor = MaterialTheme.colorScheme.onErrorContainer
                )
            ) {
                Text(text = text, style = MaterialTheme.typography.labelLarge)
            }
        }

        BeMyPetDialogActionStyle.Primary -> {
            Button(
                onClick = onClick,
                modifier = modifier.height(44.dp),
                enabled = enabled,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary,
                    disabledContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                    disabledContentColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
            ) {
                Text(text = text, style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@Composable
fun BeMyPetConfirmDialog(
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    onDismissRequest: () -> Unit = onDismiss,
    confirmText: String = "확인",
    dismissText: String = "취소",
    confirmActionStyle: BeMyPetDialogActionStyle = BeMyPetDialogActionStyle.Primary
) {
    BeMyPetDialogContainer(
        onDismissRequest = onDismissRequest,
        title = title,
        modifier = modifier,
        message = message,
        actions = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                BeMyPetDialogActionButton(
                    text = dismissText,
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    style = BeMyPetDialogActionStyle.Secondary
                )
                BeMyPetDialogActionButton(
                    text = confirmText,
                    onClick = onConfirm,
                    modifier = Modifier.weight(1f),
                    style = confirmActionStyle
                )
            }
        }
    )
}
