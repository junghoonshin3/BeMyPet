package kr.sjh.setting.screen

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import kotlinx.coroutines.flow.collectLatest
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
import kr.sjh.core.model.setting.SettingType

private const val DELETE_ACCOUNT_SESSION_EXPIRED_MESSAGE = "로그인 상태가 만료되었어요. 다시 로그인 후 시도해 주세요."
private const val TAG = "SettingScreen"

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
    val uiState by viewModel.profileUiState.collectAsStateWithLifecycle()
    val authenticatedUserId = (session as? SessionState.Authenticated)?.user?.id

    LaunchedEffect(authenticatedUserId) {
        authenticatedUserId?.let { viewModel.loadProfile(userId = it) }
    }
    LaunchedEffect(isDarkTheme) {
        viewModel.syncTheme(isDarkTheme)
    }
    LaunchedEffect(session is SessionState.Authenticated) {
        if (session !is SessionState.Authenticated) {
            viewModel.clearTransientUiState()
        }
    }
    LaunchedEffect(Unit) {
        viewModel.uiEvent.collectLatest { event ->
            when (event) {
                is SettingUiEvent.ShowMessage -> SnackBarManager.showMessage(event.message)
            }
        }
    }

    SettingScreen(
        modifier = modifier,
        session = session,
        uiState = uiState,
        onThemeSelected = { selectedTheme ->
            viewModel.selectTheme(selectedTheme)
            onChangeDarkTheme(selectedTheme == SettingType.DARK_THEME)
        },
        onNavigateToSignIn = onNavigateToSignIn,
        onSignOut = {
            coroutineScope.launch {
                accountManager.signOut()
                viewModel.signOut()
            }
        },
        onShowDeleteUserDialog = viewModel::showDeleteUserDialog,
        onHideDeleteUserDialog = viewModel::hideDeleteUserDialog,
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
                    val message = it.message ?: "회원탈퇴에 실패했어요."
                    if (isSessionExpiredDeleteError(it)) {
                        coroutineScope.launch {
                            viewModel.signOut()
                            accountManager.signOut()
                            SnackBarManager.showMessage(message)
                            onNavigateToSignIn()
                        }
                    } else {
                        SnackBarManager.showMessage(message)
                    }
                }
            )
        },
        onNavigateToBlockedUser = onNavigateToBlockedUser,
        onNavigateToMyComments = onNavigateToMyComments,
        onStartProfileEdit = viewModel::startProfileEdit,
        onProfileEditNameInputChange = viewModel::updateProfileEditNameInput,
        onProfileEditVisibleChange = viewModel::setProfileEditVisible,
        onDismissProfileEdit = viewModel::dismissProfileEdit,
        onProfileAvatarPicked = viewModel::setPickedAvatar,
        onReopenProfileEditIfNeeded = viewModel::reopenProfileEditDialogIfNeeded,
        onUpdateProfile = { userId, displayName, avatarBytes, currentAvatarUrl ->
            viewModel.updateProfileWithAvatar(
                userId = userId,
                displayName = displayName,
                avatarBytes = avatarBytes,
                currentAvatarUrl = currentAvatarUrl,
            )
        },
        onPushOptInChange = { enabled ->
            viewModel.setPushOptIn(
                enabled = enabled,
                userId = authenticatedUserId,
            )
        }
    )
}

@Composable
fun SettingScreen(
    modifier: Modifier = Modifier,
    session: SessionState,
    uiState: ProfileUiState,
    onThemeSelected: (SettingType) -> Unit,
    onNavigateToSignIn: () -> Unit,
    onSignOut: () -> Unit,
    onShowDeleteUserDialog: () -> Unit,
    onHideDeleteUserDialog: () -> Unit,
    onDeleteAccount: (String) -> Unit,
    onNavigateToBlockedUser: (String) -> Unit,
    onNavigateToMyComments: (String) -> Unit,
    onStartProfileEdit: (String, String, String?) -> Unit,
    onProfileEditNameInputChange: (String) -> Unit,
    onProfileEditVisibleChange: (Boolean) -> Unit,
    onDismissProfileEdit: (Boolean) -> Unit,
    onProfileAvatarPicked: (Uri, ByteArray?) -> Unit,
    onReopenProfileEditIfNeeded: () -> Unit,
    onUpdateProfile: (String, String, ByteArray?, String?) -> Unit,
    onPushOptInChange: (Boolean) -> Unit,
) {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val context = LocalContext.current
    val profile = uiState.profile
    val profileEditDraft = uiState.profileEditDraft

    fun hideKeyboardAndFocus() {
        focusManager.clearFocus(force = true)
        keyboardController?.hide()
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            val compressedBytes = runCatching {
                AvatarImageCompressor.compress(
                    contentResolver = context.contentResolver,
                    uri = uri
                )
            }.getOrElse { throwable ->
                Log.w(TAG, "Failed to compress picked avatar image. uri=$uri", throwable)
                SnackBarManager.showMessage(throwable.message ?: "이미지를 처리할 수 없어요.")
                null
            }
            onProfileAvatarPicked(uri, compressedBytes)
        } else {
            Log.d(TAG, "Image picker cancelled or no image selected.")
        }
        onReopenProfileEditIfNeeded()
    }
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            onPushOptInChange(true)
        } else {
            onPushOptInChange(false)
            SnackBarManager.showMessage("알림 권한이 거부되어 푸시 알림을 켤 수 없어요.")
        }
    }

    if (profileEditDraft.isVisible && profileEditDraft.editingUserId != null) {
        ProfileEditDialog(
            nameInput = profileEditDraft.nameInput,
            onNameInputChange = onProfileEditNameInputChange,
            avatarPreviewModel = profileEditDraft.avatarPreviewModel,
            avatarPreviewBytes = profileEditDraft.selectedAvatarBytes,
            hasNewAvatarSelection = profileEditDraft.hasNewAvatarSelection,
            onPickAvatar = {
                hideKeyboardAndFocus()
                onProfileEditVisibleChange(false)
                runCatching { imagePickerLauncher.launch("image/*") }
                    .onFailure {
                        SnackBarManager.showMessage("갤러리 열기에 실패했어요. 다시 시도해 주세요.")
                        onReopenProfileEditIfNeeded()
                    }
            },
            onDismiss = {
                hideKeyboardAndFocus()
                onDismissProfileEdit(true)
            },
            onSave = {
                val userId = profileEditDraft.editingUserId
                val trimmedName = profileEditDraft.nameInput.trim()
                if (trimmedName.isNotBlank()) {
                    val selectedUri = profileEditDraft.selectedAvatarUri?.let(Uri::parse)
                    val avatarBytesToUpload = profileEditDraft.selectedAvatarBytes ?: selectedUri?.let { uri ->
                        runCatching {
                            AvatarImageCompressor.compress(
                                contentResolver = context.contentResolver,
                                uri = uri
                            )
                        }.onFailure { throwable ->
                            Log.w(TAG, "Retry compression failed before profile save. uri=$uri", throwable)
                            SnackBarManager.showMessage(
                                throwable.message ?: "선택한 이미지를 처리할 수 없어요. 다시 선택해 주세요."
                            )
                        }.getOrNull()
                    }
                    if (selectedUri != null && avatarBytesToUpload == null) {
                        return@ProfileEditDialog
                    }
                    onUpdateProfile(
                        userId,
                        trimmedName,
                        avatarBytesToUpload,
                        profileEditDraft.originalAvatarUrlForSave,
                    )
                    onDismissProfileEdit(true)
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
                when (session) {
                    is SessionState.Authenticated -> {
                        ProfileAccountSection(
                            userId = session.user.id,
                            displayName = profile?.displayName ?: session.user.displayName,
                            avatarUrl = profile?.avatarUrl ?: session.user.avatarUrl,
                            isDeleteUserDialogVisible = uiState.isDeleteUserDialogVisible,
                            onEditClick = {
                                val displayName = profile?.displayName ?: session.user.displayName
                                val currentAvatarUrl = profile?.avatarUrl ?: session.user.avatarUrl
                                onStartProfileEdit(session.user.id, displayName, currentAvatarUrl)
                            },
                            onShowDeleteUserDialog = onShowDeleteUserDialog,
                            onHideDeleteUserDialog = onHideDeleteUserDialog,
                            onSignOut = onSignOut,
                            onDeleteAccount = onDeleteAccount
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
                SectionCard(title = "테마") {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        SettingType.entries.forEach { type ->
                            CheckBoxButton(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                title = type.title,
                                selected = type == uiState.selectedTheme,
                                onClick = { onThemeSelected(type) }
                            )
                        }
                    }
                }
            }

            item {
                when (session) {
                    is SessionState.Authenticated -> {
                        SectionCard(title = "알림") {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "새 공고 푸시 알림 받기",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Switch(
                                    checked = uiState.pushOptIn,
                                    onCheckedChange = { enabled ->
                                        if (!enabled) {
                                            onPushOptInChange(false)
                                        } else if (hasNotificationPermission(context)) {
                                            onPushOptInChange(true)
                                        } else {
                                            notificationPermissionLauncher.launch(
                                                Manifest.permission.POST_NOTIFICATIONS
                                            )
                                        }
                                    }
                                )
                            }
                        }
                    }

                    SessionState.Initializing -> Unit
                    is SessionState.Banned, is SessionState.NoAuthenticated, SessionState.RefreshFailure -> {
                        SectionCard(title = "알림") {
                            Text(
                                text = "새 공고 푸시 알림은 회원가입 후 로그인한 사용자에게만 제공돼요.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            item {
                SectionCard(title = "약관 및 정책") {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        SelectableListItem(
                            title = "서비스이용약관",
                            showCheckIcon = false,
                            onClick = { openExternalUrl(context, SERVICE_TERMS_URL) }
                        )
                        SelectableListItem(
                            title = "개인정보처리방침",
                            showCheckIcon = false,
                            onClick = { openExternalUrl(context, PRIVACY_POLICY_URL) }
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
private fun ProfileAccountSection(
    userId: String,
    displayName: String,
    avatarUrl: String?,
    isDeleteUserDialogVisible: Boolean,
    onEditClick: () -> Unit,
    onShowDeleteUserDialog: () -> Unit,
    onHideDeleteUserDialog: () -> Unit,
    onSignOut: () -> Unit,
    onDeleteAccount: (String) -> Unit
) {
    if (isDeleteUserDialogVisible) {
        BeMyPetConfirmDialog(
            onDismissRequest = onHideDeleteUserDialog,
            title = "회원탈퇴",
            message = "회원탈퇴를 하시겠습니까?",
            confirmText = "예",
            dismissText = "아니오",
            confirmActionStyle = BeMyPetDialogActionStyle.Destructive,
            onConfirm = {
                onDeleteAccount(userId)
                onHideDeleteUserDialog()
            },
            onDismiss = onHideDeleteUserDialog
        )
    }

    SectionCard(title = "프로필 및 계정") {
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

        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
        )

        SelectableListItem(
            title = "로그아웃",
            showCheckIcon = false,
            onClick = onSignOut
        )

        Button(
            onClick = onShowDeleteUserDialog,
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
private fun ProfileEditDialog(
    nameInput: String,
    onNameInputChange: (String) -> Unit,
    avatarPreviewModel: String?,
    avatarPreviewBytes: ByteArray?,
    hasNewAvatarSelection: Boolean,
    onPickAvatar: () -> Unit,
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
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCorner12,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.75f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        AsyncImage(
                            modifier = Modifier
                                .size(92.dp)
                                .clip(CircleShape)
                                .border(
                                    width = 2.dp,
                                    color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.35f),
                                    shape = CircleShape
                                ),
                            model = avatarPreviewBytes
                                ?: avatarPreviewModel?.let(Uri::parse)
                                ?: R.drawable.animal_carnivore_cartoon_3_svgrepo_com,
                            contentDescription = "profile_preview",
                            contentScale = ContentScale.Crop
                        )

                        Text(
                            text = "선택한 사진은 저장 시 프로필에 반영돼요",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Button(
                            onClick = onPickAvatar,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp),
                            shape = RoundedCorner12,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondary,
                                contentColor = MaterialTheme.colorScheme.onSecondary
                            )
                        ) {
                            Text(
                                text = if (hasNewAvatarSelection) "프로필사진 다시 선택" else "프로필사진 변경",
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = nameInput,
                    onValueChange = onNameInputChange,
                    label = { Text("닉네임") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
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
                    onClick = onDismiss,
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

private fun isSessionExpiredDeleteError(error: Exception): Boolean {
    val message = error.message.orEmpty()
    return message.contains(DELETE_ACCOUNT_SESSION_EXPIRED_MESSAGE)
}

private fun openExternalUrl(context: Context, url: String) {
    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
}

private fun hasNotificationPermission(context: Context): Boolean {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
        return true
    }
    return ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.POST_NOTIFICATIONS
    ) == PackageManager.PERMISSION_GRANTED
}

private const val SERVICE_TERMS_URL =
    "https://chocolate-ballcap-c67.notion.site/1af950d70a6180fb9e46e95dc0d56fd2?pvs=4"
private const val PRIVACY_POLICY_URL =
    "https://chocolate-ballcap-c67.notion.site/1d7950d70a6180d0a49cd4256a282084?pvs=4"
