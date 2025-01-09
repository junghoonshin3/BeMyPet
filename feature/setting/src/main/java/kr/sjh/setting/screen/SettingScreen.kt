package kr.sjh.setting.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kr.sjh.core.designsystem.R
import kr.sjh.core.designsystem.components.BeMyPetTopAppBar
import kr.sjh.core.designsystem.components.CheckBoxButton
import kr.sjh.core.model.setting.Setting
import kr.sjh.core.model.setting.SettingType

@Composable
fun SettingRoute(
    navigateTo: () -> Unit = {}, viewModel: SettingViewModel
) {

    val setting by viewModel.setting.collectAsStateWithLifecycle()

    SettingScreen(
        modifier = Modifier.fillMaxSize(),
        navigateTo = navigateTo,
        setting = setting,
        changeSetting = viewModel::changeSetting
    )
}

@Composable
fun SettingScreen(
    modifier: Modifier = Modifier,
    setting: Setting,
    navigateTo: () -> Unit = {},
    changeSetting: (Setting) -> Unit
) {

    Column(modifier = modifier) {
        BeMyPetTopAppBar(
            modifier = Modifier
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
        LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(10.dp)) {
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
                CheckBoxButton(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    title = type.title,
                    selected = setting.theme == type.title
                ) {
                    changeSetting(setting.copy(theme = type.title))
                }
            }
        }
    }
}