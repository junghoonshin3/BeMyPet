package kr.sjh.setting.screen

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch
import kr.sjh.core.common.ads.AdMobBanner
import kr.sjh.core.common.credential.AccountManager
import kr.sjh.core.designsystem.R
import kr.sjh.core.designsystem.components.BeMyPetTopAppBar
import kr.sjh.core.designsystem.components.CheckBoxButton
import kr.sjh.core.designsystem.theme.LocalDarkTheme
import kr.sjh.core.model.SessionState
import kr.sjh.core.model.setting.SettingType

@Composable
fun SettingRoute(
    modifier: Modifier = Modifier,
    viewModel: SettingViewModel = hiltViewModel(),
    session: SessionState,
    accountManager: AccountManager,
    isDarkTheme: Boolean = LocalDarkTheme.current,
    onChangeDarkTheme: (Boolean) -> Unit,
    onNavigateToSignIn: () -> Unit,
    onNavigateToAdoption: () -> Unit,
    onNavigateToBlockedUser: (String) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    SettingScreen(modifier = modifier,
        isDarkTheme = isDarkTheme,
        session = session,
        onChangeDarkTheme = onChangeDarkTheme,
        onNavigateToSignIn = onNavigateToSignIn,
        onSignOut = {
            coroutineScope.launch {
                accountManager.signOut()
                viewModel.signOut()
            }
        },
        onDeleteAccount = { userId ->
            viewModel.deleteAccount(userId, {
                Log.d("sjh", "삭제 완료")
                coroutineScope.launch {
                    viewModel.signOut()
                    accountManager.signOut()
                    onNavigateToAdoption()
                }
            }, {})
        },
        onNavigateToBlockedUser = { id ->
            onNavigateToBlockedUser(id)
        })
}

@Composable
fun SettingScreen(
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean,
    session: SessionState,
    onChangeDarkTheme: (Boolean) -> Unit,
    onNavigateToSignIn: () -> Unit,
    onSignOut: () -> Unit,
    onDeleteAccount: (String) -> Unit,
    onNavigateToBlockedUser: (String) -> Unit
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
        AdMobBanner()
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

            item {
                when (session) {
                    is SessionState.Authenticated -> {
                        BlockedUser(onNavigateToBlockedUser = {
                            onNavigateToBlockedUser(session.user.id)
                        })

                        DeleteUser(userId = session.user.id,
                            onSignOut = onSignOut,
                            onDeleteAccount = { onDeleteAccount(session.user.id) })
                    }

                    SessionState.Initializing -> {}
                    is SessionState.Banned, is SessionState.NoAuthenticated -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Text(
                                text = "계정", style = MaterialTheme.typography.titleMedium
                            )
                        }
                        Button(
                            onClick = {
                                onNavigateToSignIn()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp),
                            colors = ButtonDefaults.textButtonColors(
                                containerColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        ) {
                            Text(text = "로그인", style = MaterialTheme.typography.titleMedium)
                        }
                    }

                    SessionState.RefreshFailure -> {}
                }
            }
            item {
                val context = LocalContext.current
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        text = "약관 및 정책", style = MaterialTheme.typography.titleMedium
                    )
                }
                Button(
                    onClick = {
                        val intent = Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("https://chocolate-ballcap-c67.notion.site/1af950d70a6180fb9e46e95dc0d56fd2?pvs=4")
                        )
                        context.startActivity(intent)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    colors = ButtonDefaults.textButtonColors(
                        containerColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                ) {
                    Text(text = "서비스이용약관", style = MaterialTheme.typography.titleMedium)
                }
                Spacer(Modifier.height(10.dp))
                Button(
                    onClick = {
                        val intent = Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("https://chocolate-ballcap-c67.notion.site/1d7950d70a6180d0a49cd4256a282084?pvs=4")
                        )
                        context.startActivity(intent)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    colors = ButtonDefaults.textButtonColors(
                        containerColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                ) {
                    Text(text = "개인정보처리방침", style = MaterialTheme.typography.titleMedium)
                }
            }
        }

    }
}

@Composable
fun DeleteUser(userId: String, onSignOut: () -> Unit, onDeleteAccount: (String) -> Unit) {
    var isDeleteUserShow by remember {
        mutableStateOf(false)
    }
    if (isDeleteUserShow) {
        AlertDialog(onDismissRequest = { },
            title = { Text("회원탈퇴") },
            text = { Text("회원탈퇴를 하시겠습니까?") },
            confirmButton = {
                TextButton(onClick = {
                    onDeleteAccount(userId)
                    isDeleteUserShow = false
                }) {
                    Text("예", color = MaterialTheme.colorScheme.onPrimary)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    // 다이얼로그만 닫고 유지
                    isDeleteUserShow = false
                }) {
                    Text("아니오", color = MaterialTheme.colorScheme.onPrimary)
                }
            })
    }
    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = "계정", style = MaterialTheme.typography.titleMedium
            )
        }

        Button(
            onClick = {
                onSignOut()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            colors = ButtonDefaults.textButtonColors(
                containerColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        ) {
            Text(text = "로그아웃", style = MaterialTheme.typography.titleMedium)

        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = { isDeleteUserShow = true },
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            colors = ButtonDefaults.textButtonColors(
                containerColor = Color.Red
            )
        ) {
            Text(text = "회원탈퇴", style = MaterialTheme.typography.titleMedium)

        }
    }
}


@Composable
fun BlockedUser(onNavigateToBlockedUser: () -> Unit) {
    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = "사용자 설정", style = MaterialTheme.typography.titleMedium
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onNavigateToBlockedUser),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                modifier = Modifier.padding(16.dp),
                text = "사용자 차단목록",
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
    }

}
