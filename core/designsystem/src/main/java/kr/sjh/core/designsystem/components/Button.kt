package kr.sjh.core.designsystem.components

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

@Composable
fun DefaultButton(
    @StringRes resourceId: Int, onClick: () -> Unit, modifier: Modifier = Modifier
) {
    Button(
        modifier = modifier.defaultMinSize(56.dp),
        onClick = {
            onClick()
        },
        shape = RoundedCornerShape(48.dp),
    ) {
        Text(
            text = stringResource(id = resourceId),
            style = MaterialTheme.typography.titleMedium,
        )
    }
}