package kr.sjh.feature.signup

import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import kr.sjh.core.common.credential.AccountManager
import kr.sjh.core.common.credential.SignInResult
import kr.sjh.core.common.snackbar.SnackBarManager
import kr.sjh.core.designsystem.R
import kr.sjh.core.designsystem.components.BeMyPetDialogActionButton
import kr.sjh.core.designsystem.components.BeMyPetDialogActionStyle
import kr.sjh.core.designsystem.components.BeMyPetDialogContainer
import kr.sjh.core.designsystem.theme.RoundedCorner12
import kr.sjh.core.model.KakaoEmailVerificationReason
import kr.sjh.core.model.SessionState

@Composable
fun SignInRoute(
    modifier: Modifier = Modifier,
    viewModel: SignInViewModel = hiltViewModel(),
    session: SessionState,
    accountManager: AccountManager,
    onSignInSuccess: () -> Unit,
    onRequireKakaoEmailVerification: (KakaoEmailVerificationReason) -> Unit,
    onBack: () -> Unit
) {
    val focusManager = LocalFocusManager.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(session) {
        when (session) {
            is SessionState.Authenticated -> onSignInSuccess()
            is SessionState.EmailVerificationRequired -> onRequireKakaoEmailVerification(session.reason)
            else -> Unit
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.takeIf { it.isNotBlank() }?.let { message ->
            SnackBarManager.showMessage(message)
            viewModel.clearError()
        }
    }

    SignInScreen(
        modifier = modifier.pointerInput(Unit) {
            detectTapGestures(onTap = { focusManager.clearFocus() })
        },
        uiState = uiState,
        accountManager = accountManager,
        onGoogleSignIn = viewModel::onGoogleSignIn,
        onKakaoSignIn = viewModel::onKakaoSignIn,
        onBack = onBack
    )
}

@Composable
private fun SignInScreen(
    modifier: Modifier = Modifier,
    uiState: SignInUiState,
    onGoogleSignIn: (String, String) -> Unit,
    onKakaoSignIn: () -> Unit,
    accountManager: AccountManager,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var showNoCredentialsDialog by remember { mutableStateOf(false) }

    val tryGoogleSignIn: () -> Unit = {
        coroutineScope.launch {
            fun handleSuccess(result: SignInResult.Success) {
                onGoogleSignIn(result.idToken, result.nonce)
            }

            when (val result = accountManager.signIn()) {
                SignInResult.Cancelled -> {
                    SnackBarManager.showMessage("로그인이 취소되었어요. 다시 시도해 주세요.")
                }

                is SignInResult.Failure -> {
                    Log.w(SIGN_IN_TAG, "Google sign-in failed.", result.e)
                    SnackBarManager.showMessage(mapGoogleFailureMessage(result.e))
                }

                is SignInResult.Success -> {
                    handleSuccess(result)
                }

                SignInResult.NoCredentials -> {
                    showNoCredentialsDialog = true
                }
            }
        }
    }

    if (showNoCredentialsDialog) {
        NoCredentialsDialog(
            onRetry = tryGoogleSignIn,
            onOpenSettings = {
                showNoCredentialsDialog = false
                openGoogleSettings(context)
            },
            onDismiss = { showNoCredentialsDialog = false }
        )
    }

    BackHandler(onBack = onBack)

    SignInReferenceLayout(
        modifier = modifier,
        uiState = uiState,
        onGoogleSignInClick = tryGoogleSignIn,
        onKakaoClick = onKakaoSignIn
    )
}

@Composable
private fun SignInReferenceLayout(
    modifier: Modifier = Modifier,
    uiState: SignInUiState,
    onGoogleSignInClick: () -> Unit,
    onKakaoClick: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .imePadding()
    ) {
        SignInHeroSection(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.42f)
        )
        SignInFormSection(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.58f),
            uiState = uiState,
            onGoogleSignInClick = onGoogleSignInClick,
            onKakaoClick = onKakaoClick
        )
    }
}

@Composable
private fun SignInHeroSection(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.mipmap.ic_launcher_foreground),
            contentDescription = null,
            modifier = Modifier.size(140.dp)
        )
    }
}

@Composable
private fun SignInFormSection(
    modifier: Modifier = Modifier,
    uiState: SignInUiState,
    onGoogleSignInClick: () -> Unit,
    onKakaoClick: () -> Unit
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
            .background(MaterialTheme.colorScheme.surface)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "반가워요!",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface
        )

        Text(
            text = "소셜 계정으로 빠르게 시작해요.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            HorizontalDivider(
                modifier = Modifier.weight(1f),
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.outline
            )
            Text(
                text = "소셜 로그인",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            HorizontalDivider(
                modifier = Modifier.weight(1f),
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.outline
            )
        }

        SocialLoginButtons(
            uiState = uiState,
            onGoogleSignInClick = onGoogleSignInClick,
            onKakaoClick = onKakaoClick
        )
    }
}

@Composable
private fun SocialLoginButtons(
    uiState: SignInUiState,
    onGoogleSignInClick: () -> Unit,
    onKakaoClick: () -> Unit
) {
    val isGoogleLoading = uiState.loadingProvider == LoadingProvider.Google
    val isKakaoLoading = uiState.loadingProvider == LoadingProvider.Kakao

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Button(
            onClick = onGoogleSignInClick,
            enabled = !uiState.isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCorner12,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onSecondary,
                disabledContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                disabledContentColor = MaterialTheme.colorScheme.onSecondaryContainer
            )
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "G",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSecondary
                    )
                }
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.width(10.dp))
                if (isGoogleLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onSecondary
                    )
                    androidx.compose.foundation.layout.Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "처리 중...",
                        style = MaterialTheme.typography.labelLarge
                    )
                } else {
                    Text(
                        text = "Google로 계속하기",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }

        Button(
            onClick = onKakaoClick,
            enabled = !uiState.isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCorner12),
            shape = RoundedCorner12,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.kakaotalk_logo),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = Color.Unspecified
                )
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.width(10.dp))
                if (isKakaoLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    androidx.compose.foundation.layout.Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "처리 중...",
                        style = MaterialTheme.typography.labelLarge
                    )
                } else {
                    Text(
                        text = "카카오로 계속하기",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}

@Composable
private fun NoCredentialsDialog(
    onRetry: () -> Unit,
    onOpenSettings: () -> Unit,
    onDismiss: () -> Unit,
) {
    BeMyPetDialogContainer(
        onDismissRequest = onDismiss,
        title = "Google 로그인 정보를 불러오지 못했어요",
        message = "다시 시도하거나 설정에서 Google 계정 동기화를 확인해 주세요.",
        actions = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                BeMyPetDialogActionButton(
                    text = "다시 시도",
                    onClick = onRetry,
                    modifier = Modifier.weight(1f),
                    style = BeMyPetDialogActionStyle.Primary
                )
                BeMyPetDialogActionButton(
                    text = "설정 열기",
                    onClick = onOpenSettings,
                    modifier = Modifier.weight(1f),
                    style = BeMyPetDialogActionStyle.Secondary
                )
            }
            BeMyPetDialogActionButton(
                text = "취소",
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
                style = BeMyPetDialogActionStyle.Secondary
            )
        }
    )
}

private fun mapGoogleFailureMessage(exception: Exception): String {
    val loweredMessage = exception.message?.lowercase().orEmpty()
    return when {
        "developer_error" in loweredMessage ||
            "invalid_audience" in loweredMessage ||
            "audience" in loweredMessage && "client" in loweredMessage ||
            "10:" in loweredMessage -> {
            "로그인 설정이 맞지 않아 관리자 확인이 필요해요."
        }

        else -> "Google 로그인에 실패했어요. 잠시 후 다시 시도해 주세요."
    }
}

private fun openGoogleSettings(context: Context) {
    val syncSettingsIntent = Intent(Settings.ACTION_SYNC_SETTINGS).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    val fallbackSettingsIntent = Intent(Settings.ACTION_SETTINGS).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    runCatching {
        context.startActivity(syncSettingsIntent)
    }.onFailure { syncError ->
        Log.w(SIGN_IN_TAG, "Failed to open sync settings. Trying fallback settings.", syncError)
        runCatching {
            context.startActivity(fallbackSettingsIntent)
        }.onFailure { fallbackError ->
            Log.e(SIGN_IN_TAG, "Failed to open settings for Google credential recovery.", fallbackError)
            SnackBarManager.showMessage("설정 화면을 열 수 없어요.")
        }
    }
}

private const val SIGN_IN_TAG = "SignInScreen"
