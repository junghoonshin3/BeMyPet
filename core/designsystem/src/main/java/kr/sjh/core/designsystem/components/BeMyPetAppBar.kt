package kr.sjh.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun BeMyPetTopAppBar(
    title: @Composable () -> Unit = {},
    content: @Composable () -> Unit = {},
    iconButton : @Composable ()->Unit={},
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.then(Modifier.heightIn(min = 64.dp)),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.Start
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Start) {
            title()
            Spacer(modifier = Modifier.weight(1f))
            iconButton()
        }

        content()
    }
}
