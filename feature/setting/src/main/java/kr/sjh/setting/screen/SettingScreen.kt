package kr.sjh.setting.screen

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
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
import kr.sjh.core.designsystem.theme.RoundedCorner12
import kr.sjh.core.designsystem.theme.RoundedCorner18
import kr.sjh.core.designsystem.theme.RoundedCornerBottom24
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
    onNavigateToBlockedUser: (String) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val profileUiState by viewModel.profileUiState.collectAsStateWithLifecycle()

    LaunchedEffect(session) {
        if (session is SessionState.Authenticated) {
            viewModel.loadProfile(session.user.id)
        }
    }

    SettingScreen(
        modifier = modifier,
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
        }
    )
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

    Column(
        modifier = modifier.background(MaterialTheme.colorScheme.background)
    ) {
        BeMyPetTopAppBar(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(4.dp, RoundedCornerBottom24)
                .background(MaterialTheme.colorScheme.primary, RoundedCornerBottom24)
                .clip(RoundedCornerBottom24),
            title = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "계정과 앱 환경을 정리해요",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = stringResource(R.string.setting),
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        )
        AdMobBanner()
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                SectionCard(title = "테마 변경") {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        SettingType.entries.forEach { type ->
                            CheckBoxButton(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                title = type.title,
                                selected = type == selectedTheme,
                                onClick = {
                                    selectedTheme = type
                                    onChangeDarkTheme(selectedTheme == SettingType.DARK_THEME)
                                }
                            )
                        }
                    }
                }
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
                        Spacer(modifier = Modifier.height(14.dp))
                        BlockedUser(
                            onNavigateToBlockedUser = {
                                onNavigateToBlockedUser(session.user.id)
                            }
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        DeleteUser(
                            userId = session.user.id,
                            onSignOut = onSignOut,
                            onDeleteAccount = { onDeleteAccount(session.user.id) }
                        )
                    }

                    SessionState.Initializing -> {}
                    is SessionState.Banned, is SessionState.NoAuthenticated -> {
                        SectionCard(title = "계정") {
                            Button(
                                onClick = {
                                    onNavigateToSignIn()
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(54.dp),
                                shape = RoundedCorner12,
                                colors = ButtonDefaults.textButtonColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                                )
                            ) {
                                Text(
                                    text = "로그인",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                    }

                    SessionState.RefreshFailure -> {}
                }
            }

            item {
                val context = LocalContext.current
                SectionCard(title = "약관 및 정책") {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        PolicyButton(
                            text = "서비스이용약관",
                            onClick = {
                                val intent = Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("https://chocolate-ballcap-c67.notion.site/1af950d70a6180fb9e46e95dc0d56fd2?pvs=4")
                                )
                                context.startActivity(intent)
                            }
                        )
                        PolicyButton(
                            text = "개인정보처리방침",
                            onClick = {
                                val intent = Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("https://chocolate-ballcap-c67.notion.site/1d7950d70a6180d0a49cd4256a282084?pvs=4")
                                )
                                context.startActivity(intent)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionCard(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCorner18,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.7f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            content()
        }
    }
}

@Composable
private fun PolicyButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = RoundedCorner12,
        colors = ButtonDefaults.textButtonColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
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
            title = {
                Text(
                    text = "프로필 수정",
                    style = MaterialTheme.typography.titleMedium
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        label = { Text("닉네임") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
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
                TextButton(
                    onClick = {
                        val name = nameInput.trim()
                        if (name.isNotBlank()) {
                            onUpdateProfile(userId, name, avatarInput.trim().ifBlank { null })
                            showEditDialog = false
                        }
                    }
                ) {
                    Text("저장", color = MaterialTheme.colorScheme.onSurface)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("취소", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        )
    }

    SectionCard(title = "프로필") {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape),
                model = avatarUrl ?: R.drawable.animal_carnivore_cartoon_3_svgrepo_com,
                contentDescription = "avatar",
                contentScale = ContentScale.Crop
            )
            Text(
                modifier = Modifier
                    .padding(start = 12.dp)
                    .weight(1f),
                text = displayName.ifBlank { "닉네임 미설정" },
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Button(
            onClick = { showEditDialog = true },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCorner12,
            colors = ButtonDefaults.textButtonColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Text(
                text = "프로필 수정",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

@Composable
fun DeleteUser(userId: String, onSignOut: () -> Unit, onDeleteAccount: (String) -> Unit) {
    var isDeleteUserShow by remember {
        mutableStateOf(false)
    }

    if (isDeleteUserShow) {
        AlertDialog(
            onDismissRequest = { isDeleteUserShow = false },
            title = { Text("회원탈퇴") },
            text = { Text("회원탈퇴를 하시겠습니까?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteAccount(userId)
                        isDeleteUserShow = false
                    }
                ) {
                    Text("예", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { isDeleteUserShow = false }) {
                    Text("아니오", color = MaterialTheme.colorScheme.onSurface)
                }
            }
        )
    }

    SectionCard(title = "계정") {
        Button(
            onClick = {
                onSignOut()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCorner12,
            colors = ButtonDefaults.textButtonColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Text(
                text = "로그아웃",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Button(
            onClick = { isDeleteUserShow = true },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCorner12,
            colors = ButtonDefaults.textButtonColors(
                containerColor = MaterialTheme.colorScheme.error
            )
        ) {
            Text(
                text = "회원탈퇴",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onError
            )
        }
    }
}

@Composable
fun BlockedUser(onNavigateToBlockedUser: () -> Unit) {
    SectionCard(title = "사용자 설정") {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCorner12)
                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCorner12)
                .clickable(onClick = onNavigateToBlockedUser),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                text = "사용자 차단목록",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
