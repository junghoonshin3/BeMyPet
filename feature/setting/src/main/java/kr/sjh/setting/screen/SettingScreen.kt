package kr.sjh.setting.screen

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
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
import kr.sjh.core.designsystem.components.BeMyPetConfirmDialog
import kr.sjh.core.designsystem.components.BeMyPetDialogActionButton
import kr.sjh.core.designsystem.components.BeMyPetDialogActionStyle
import kr.sjh.core.designsystem.components.BeMyPetDialogContainer
import kr.sjh.core.designsystem.components.BeMyPetTopAppBar
import kr.sjh.core.designsystem.components.CheckBoxButton
import kr.sjh.core.designsystem.components.PrimaryActionButton
import kr.sjh.core.designsystem.components.SelectableListItem
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
    onNavigateToBlockedUser: (String) -> Unit,
    onNavigateToMyComments: (String) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val profileUiState by viewModel.profileUiState.collectAsStateWithLifecycle()
    val authenticatedUserId = (session as? SessionState.Authenticated)?.user?.id

    LaunchedEffect(authenticatedUserId) {
        authenticatedUserId?.let { viewModel.loadProfile(userId = it) }
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
            viewModel.deleteAccount(
                userId = userId,
                onSuccess = {
                    coroutineScope.launch {
                        viewModel.signOut()
                        accountManager.signOut()
                        onNavigateToAdoption()
                    }
                },
                onFailure = {
                    SnackBarManager.showMessage(it.message ?: "회원탈퇴에 실패했어요.")
                }
            )
        },
        onNavigateToBlockedUser = onNavigateToBlockedUser,
        onNavigateToMyComments = onNavigateToMyComments,
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
    onNavigateToMyComments: (String) -> Unit,
    onUpdateProfile: (String, String, String?) -> Unit
) {
    var selectedTheme by remember(isDarkTheme) {
        mutableStateOf(if (isDarkTheme) SettingType.DARK_THEME else SettingType.LIGHT_THEME)
    }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val avatarFieldFocusRequester = remember { FocusRequester() }
    var showProfileEditDialog by rememberSaveable { mutableStateOf(false) }
    var editingUserId by rememberSaveable { mutableStateOf<String?>(null) }
    var nameInput by rememberSaveable { mutableStateOf("") }
    var avatarInput by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(session is SessionState.Authenticated) {
        if (session !is SessionState.Authenticated) {
            showProfileEditDialog = false
            editingUserId = null
        }
    }

    if (showProfileEditDialog && editingUserId != null) {
        ProfileEditDialog(
            nameInput = nameInput,
            onNameInputChange = { nameInput = it },
            avatarInput = avatarInput,
            onAvatarInputChange = { avatarInput = it },
            avatarFieldFocusRequester = avatarFieldFocusRequester,
            onDismiss = {
                focusManager.clearFocus(force = true)
                keyboardController?.hide()
                showProfileEditDialog = false
            },
            onSave = {
                val userId = editingUserId ?: return@ProfileEditDialog
                val trimmedName = nameInput.trim()
                if (trimmedName.isNotBlank()) {
                    onUpdateProfile(
                        userId,
                        trimmedName,
                        avatarInput.trim().ifBlank { null }
                    )
                    focusManager.clearFocus(force = true)
                    keyboardController?.hide()
                    showProfileEditDialog = false
                }
            }
        )
    }

    Column(modifier = modifier.background(MaterialTheme.colorScheme.background)) {
        BeMyPetTopAppBar(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primary, RoundedCornerBottom24)
                .clip(RoundedCornerBottom24),
            title = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "앱 환경과 계정을 관리해요",
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
                SectionCard(title = "테마") {
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
                            displayName = profile?.displayName ?: session.user.displayName,
                            avatarUrl = profile?.avatarUrl ?: session.user.avatarUrl,
                            onEditClick = {
                                editingUserId = session.user.id
                                nameInput = profile?.displayName ?: session.user.displayName
                                avatarInput = profile?.avatarUrl ?: session.user.avatarUrl.orEmpty()
                                showProfileEditDialog = true
                            }
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        BlockedUser(
                            onNavigateToBlockedUser = {
                                onNavigateToBlockedUser(session.user.id)
                            }
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        MyActivitySection(
                            onNavigateToMyComments = {
                                onNavigateToMyComments(session.user.id)
                            }
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        DeleteUser(
                            userId = session.user.id,
                            onSignOut = onSignOut,
                            onDeleteAccount = { onDeleteAccount(session.user.id) }
                        )
                    }

                    SessionState.Initializing -> Unit
                    is SessionState.Banned, is SessionState.NoAuthenticated -> {
                        SectionCard(title = "계정") {
                            PrimaryActionButton(
                                text = "로그인",
                                modifier = Modifier.fillMaxWidth(),
                                onClick = onNavigateToSignIn
                            )
                        }
                    }

                    SessionState.RefreshFailure -> Unit
                }
            }

            item {
                val context = LocalContext.current
                SectionCard(title = "약관 및 정책") {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        SelectableListItem(
                            title = "서비스이용약관",
                            showCheckIcon = false,
                            onClick = {
                                val intent = Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("https://chocolate-ballcap-c67.notion.site/1af950d70a6180fb9e46e95dc0d56fd2?pvs=4")
                                )
                                context.startActivity(intent)
                            }
                        )
                        SelectableListItem(
                            title = "개인정보처리방침",
                            showCheckIcon = false,
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
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.8f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
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
private fun ProfileSection(
    displayName: String,
    avatarUrl: String?,
    onEditClick: () -> Unit
) {
    SectionCard(title = "프로필") {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                modifier = Modifier
                    .size(56.dp)
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

        SelectableListItem(
            title = "프로필 수정",
            selected = true,
            showCheckIcon = false,
            onClick = onEditClick
        )
    }
}

@Composable
private fun ProfileEditDialog(
    nameInput: String,
    onNameInputChange: (String) -> Unit,
    avatarInput: String,
    onAvatarInputChange: (String) -> Unit,
    avatarFieldFocusRequester: FocusRequester,
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    BeMyPetDialogContainer(
        onDismissRequest = onDismiss,
        title = "프로필 수정",
        content = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = nameInput,
                    onValueChange = onNameInputChange,
                    label = { Text("닉네임") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCorner12,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(
                        onNext = { avatarFieldFocusRequester.requestFocus() }
                    )
                )
                OutlinedTextField(
                    value = avatarInput,
                    onValueChange = onAvatarInputChange,
                    label = { Text("아바타 URL") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(avatarFieldFocusRequester),
                    shape = RoundedCorner12,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            focusManager.clearFocus(force = true)
                            keyboardController?.hide()
                        }
                    )
                )
            }
        },
        actions = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                BeMyPetDialogActionButton(
                    text = "취소",
                    onClick = {
                        focusManager.clearFocus(force = true)
                        keyboardController?.hide()
                        onDismiss()
                    },
                    modifier = Modifier.weight(1f),
                    style = BeMyPetDialogActionStyle.Secondary
                )
                BeMyPetDialogActionButton(
                    text = "저장",
                    onClick = onSave,
                    modifier = Modifier.weight(1f),
                    style = BeMyPetDialogActionStyle.Primary
                )
            }
        }
    )
}

@Composable
fun DeleteUser(userId: String, onSignOut: () -> Unit, onDeleteAccount: (String) -> Unit) {
    var isDeleteUserShow by remember {
        mutableStateOf(false)
    }

    if (isDeleteUserShow) {
        BeMyPetConfirmDialog(
            onDismissRequest = { isDeleteUserShow = false },
            title = "회원탈퇴",
            message = "회원탈퇴를 하시겠습니까?",
            confirmText = "예",
            dismissText = "아니오",
            confirmActionStyle = BeMyPetDialogActionStyle.Destructive,
            onConfirm = {
                onDeleteAccount(userId)
                isDeleteUserShow = false
            },
            onDismiss = { isDeleteUserShow = false }
        )
    }

    SectionCard(title = "계정") {
        SelectableListItem(
            title = "로그아웃",
            showCheckIcon = false,
            onClick = onSignOut
        )

        Button(
            onClick = { isDeleteUserShow = true },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCorner12,
            colors = ButtonDefaults.textButtonColors(
                containerColor = MaterialTheme.colorScheme.error
            )
        ) {
            Text(
                text = "회원탈퇴",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onError
            )
        }
    }
}

@Composable
fun BlockedUser(onNavigateToBlockedUser: () -> Unit) {
    SectionCard(title = "사용자 설정") {
        SelectableListItem(
            title = "사용자 차단목록",
            showCheckIcon = false,
            onClick = onNavigateToBlockedUser
        )
    }
}

@Composable
fun MyActivitySection(onNavigateToMyComments: () -> Unit) {
    SectionCard(title = "내 활동") {
        SelectableListItem(
            title = "내가 쓴 댓글",
            showCheckIcon = false,
            onClick = onNavigateToMyComments
        )
    }
}
