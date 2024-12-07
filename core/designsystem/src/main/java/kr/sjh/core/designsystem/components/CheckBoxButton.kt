package kr.sjh.core.designsystem.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kr.sjh.core.designsystem.R

@Composable
fun CheckBoxButton(
    modifier: Modifier = Modifier, title: String, selected: Boolean, onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .clickable {
                onClick()
            }
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 16.dp)
            .then(modifier),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = title, fontSize = 15.sp)
        Spacer(modifier = Modifier.weight(1f))
        Box(modifier = Modifier.size(30.dp)) {
            Icon(
                imageVector = ImageVector.vectorResource(id = R.drawable.check_circle_svgrepo_com),
                contentDescription = "check",
                tint = if (selected) Color.Red else Color.LightGray
            )
        }
    }
}

@Composable
@Preview
private fun RadioButtonPreview() {
    var selected by remember {
        mutableStateOf(false)
    }
    CheckBoxButton(title = "개", selected = selected, onClick = {
        selected = !selected
    })
}
