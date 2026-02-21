package kr.sjh.core.designsystem.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kr.sjh.core.designsystem.theme.DefaultAppBarHeight

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
