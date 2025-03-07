package kr.sjh.setting.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kr.sjh.core.common.ads.AdMobBanner
import kr.sjh.core.designsystem.R
import kr.sjh.core.designsystem.components.BeMyPetTopAppBar
import kr.sjh.core.designsystem.components.CheckBoxButton
import kr.sjh.core.designsystem.theme.LocalDarkTheme
import kr.sjh.core.model.setting.SettingType

@Composable
fun SettingRoute(
    isDarkTheme: Boolean = LocalDarkTheme.current, onChangeDarkTheme: (Boolean) -> Unit
) {
    SettingScreen(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        isDarkTheme = isDarkTheme,
        onChangeDarkTheme = onChangeDarkTheme
    )
}

@Composable
fun SettingScreen(
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean,
    onChangeDarkTheme: (Boolean) -> Unit,
) {

    var selectedTheme by remember(isDarkTheme) {
        mutableStateOf(if (isDarkTheme) SettingType.DARK_THEME else SettingType.LIGHT_THEME)
    }

    Column(modifier = modifier) {
        BeMyPetTopAppBar(modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primary),
            title = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        text = stringResource(R.string.setting),
                        style = MaterialTheme.typography.headlineSmall
                    )
                }
            })
        LazyColumn(modifier = Modifier.weight(1f), contentPadding = PaddingValues(10.dp)) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        text = "테마 변경", style = MaterialTheme.typography.titleMedium
                    )
                }
            }
            items(SettingType.entries.toTypedArray()) { type ->
                CheckBoxButton(modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                    title = type.title,
                    selected = type == selectedTheme,
                    onClick = {
                        selectedTheme = type
                        onChangeDarkTheme(selectedTheme == SettingType.DARK_THEME)
                    })
            }
        }
        AdMobBanner()
    }
}