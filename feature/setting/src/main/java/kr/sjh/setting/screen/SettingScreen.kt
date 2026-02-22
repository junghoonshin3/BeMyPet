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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import kr.sjh.core.common.ads.AdMobBanner
import kr.sjh.core.common.credential.AccountManager
import kr.sjh.core.common.snackbar.SnackBarManager
import kr.sjh.core.designsystem.R
import kr.sjh.core.designsystem.components.BeMyPetTopAppBar
import kr.sjh.core.designsystem.components.CheckBoxButton
import kr.sjh.core.designsystem.theme.LocalDarkTheme
import kr.sjh.core.model.SessionState
import kr.sjh.core.model.UserProfile
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
    onNavigateToBlockedUser: (String) -> Unit,
    onNavigateToMyComments: (String) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val profileUiState by viewModel.profileUiState.collectAsStateWithLifecycle()

    LaunchedEffect(session) {
        if (session is SessionState.Authenticated) {
            viewModel.loadProfile(session.user.id)
        }
    }

    SettingScreen(modifier = modifier,
        isDarkTheme = isDarkTheme,
        session = session,
        profile = profileUiState.profile,
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
        },
        onUpdateProfile = { userId, displayName, avatarUrl ->
            viewModel.updateProfile(
                userId = userId,
                displayName = displayName,
                avatarUrl = avatarUrl,
                onSuccess = {
                    SnackBarManager.showMessage("프로필을 업데이트했어요.")
                },
                onFailure = {
                    SnackBarManager.showMessage(it.message ?: "프로필 업데이트에 실패했어요.")
                }
            )
        })
}

@Composable
fun SettingScreen(
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean,
    session: SessionState,
    profile: UserProfile?,
    onChangeDarkTheme: (Boolean) -> Unit,
    onNavigateToSignIn: () -> Unit,
    onSignOut: () -> Unit,
    onDeleteAccount: (String) -> Unit,
    onNavigateToBlockedUser: (String) -> Unit,
    onUpdateProfile: (String, String, String?) -> Unit
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
                        ProfileSection(
                            userId = session.user.id,
                            displayName = profile?.displayName ?: session.user.displayName,
                            avatarUrl = profile?.avatarUrl ?: session.user.avatarUrl,
                            onUpdateProfile = onUpdateProfile
                        )
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
private fun ProfileSection(
    userId: String,
    displayName: String,
    avatarUrl: String?,
    onUpdateProfile: (String, String, String?) -> Unit
) {
    var showEditDialog by remember { mutableStateOf(false) }

    if (showEditDialog) {
        var nameInput by remember(displayName) { mutableStateOf(displayName) }
        var avatarInput by remember(avatarUrl) { mutableStateOf(avatarUrl.orEmpty()) }

        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("프로필 수정") },
            text = {
                Column {
                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        label = { Text("닉네임") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = avatarInput,
                        onValueChange = { avatarInput = it },
                        label = { Text("아바타 URL") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val name = nameInput.trim()
                    if (name.isNotBlank()) {
                        onUpdateProfile(userId, name, avatarInput.trim().ifBlank { null })
                        showEditDialog = false
                    }
                }) {
                    Text("저장", color = MaterialTheme.colorScheme.onPrimary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("취소", color = MaterialTheme.colorScheme.onPrimary)
                }
            }
        )
    }

    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = "프로필", style = MaterialTheme.typography.titleMedium
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            AsyncImage(
                modifier = Modifier
                    .size(56.dp)
                    .clip(androidx.compose.foundation.shape.CircleShape),
                model = avatarUrl ?: R.drawable.animal_carnivore_cartoon_3_svgrepo_com,
                contentDescription = "avatar",
                contentScale = ContentScale.Crop
            )
            Text(
                modifier = Modifier.padding(start = 72.dp),
                text = displayName.ifBlank { "닉네임 미설정" },
                style = MaterialTheme.typography.titleMedium
            )
        }
        Button(
            onClick = { showEditDialog = true },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.textButtonColors(
                containerColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        ) {
            Text(text = "프로필 수정", style = MaterialTheme.typography.titleMedium)
        }
        Spacer(modifier = Modifier.height(8.dp))
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
