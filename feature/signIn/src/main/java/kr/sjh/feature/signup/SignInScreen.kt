package kr.sjh.feature.signup

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import kr.sjh.core.common.credential.AccountManager
import kr.sjh.core.common.credential.SignInResult
import kr.sjh.core.common.snackbar.SnackBarManager
import kr.sjh.core.designsystem.R
import kr.sjh.core.designsystem.components.LoadingComponent
import kr.sjh.core.designsystem.theme.RoundedCorner12
import kr.sjh.core.designsystem.theme.RoundedCorner18

private enum class SignInTab {
    GOOGLE,
    EMAIL,
}

@Composable
fun SignInRoute(
    modifier: Modifier = Modifier,
    viewModel: SignInViewModel = hiltViewModel(),
    accountManager: AccountManager,
    onSignInSuccess: () -> Unit,
    onBack: () -> Unit
) {
    val focusManager = LocalFocusManager.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.isSignedIn) {
        if (uiState.isSignedIn) {
            onSignInSuccess()
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
        onSignIn = viewModel::onSignIn,
        onBack = onBack
    )
}

@Composable
private fun SignInScreen(
    modifier: Modifier = Modifier,
    uiState: SignInUiState,
    onSignIn: (SignUpModel) -> Unit,
    accountManager: AccountManager,
    onBack: () -> Unit
) {
    val googleLoginImage = ImageVector.vectorResource(R.drawable.android_light_rd_ctn)
    val coroutineScope = rememberCoroutineScope()

    var selectedTab by remember { mutableStateOf(SignInTab.GOOGLE) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "닫기",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.size(48.dp))
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            modifier = Modifier.fillMaxWidth(),
            text = "로그인",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            modifier = Modifier.fillMaxWidth(),
            text = "따뜻한 입양 커뮤니티를 시작해보세요",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(20.dp))

        TabRow(
            selectedTabIndex = selectedTab.ordinal,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ) {
            SignInTab.entries.forEachIndexed { index, tab ->
                Tab(
                    selected = selectedTab.ordinal == index,
                    onClick = { selectedTab = tab },
                    text = {
                        Text(
                            text = if (tab == SignInTab.GOOGLE) "Google" else "Email",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (selectedTab) {
            SignInTab.GOOGLE -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(76.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.secondaryContainer,
                                    shape = RoundedCornerShape(38.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                modifier = Modifier.size(34.dp),
                                imageVector = ImageVector.vectorResource(
                                    R.drawable.animal_carnivore_cartoon_3_svgrepo_com
                                ),
                                contentDescription = "logo"
                            )
                        }

                        Text(
                            text = "간편 로그인으로 빠르게 시작",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Text(
                            text = "기존 Google 계정으로 안전하게 로그인할 수 있어요.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (uiState.isLoading) {
                        LoadingComponent()
                    } else {
                        SocialLoginButton(
                            modifier = Modifier
                                .width(300.dp)
                                .height(60.dp),
                            imageVector = googleLoginImage,
                            onClick = {
                                coroutineScope.launch {
                                    when (val result = accountManager.signIn(false)) {
                                        SignInResult.Cancelled -> Unit
                                        is SignInResult.Failure -> {
                                            SnackBarManager.showMessage(
                                                result.e.message ?: "Google 로그인에 실패했어요."
                                            )
                                        }

                                        SignInResult.NoCredentials -> {
                                            accountManager.signIn(false)
                                        }

                                        is SignInResult.Success -> {
                                            onSignIn(
                                                SignUpModel(
                                                    result.idToken,
                                                    result.nonce,
                                                    "google"
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        )
                    }
                }
            }

            SignInTab.EMAIL -> {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCorner18)
                        .background(MaterialTheme.colorScheme.surface)
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.7f),
                            shape = RoundedCorner18
                        )
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Email 로그인",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("이메일") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        )
                    )

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("비밀번호") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        visualTransformation = PasswordVisualTransformation()
                    )

                    Text(
                        text = "Email/Password 로그인은 곧 지원될 예정이에요.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Button(
                        enabled = false,
                        onClick = {
                            onSignIn(SignUpModel("", "", "email"))
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCorner12,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    ) {
                        Text(
                            text = "준비중",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun SocialLoginButton(
    modifier: Modifier = Modifier,
    imageVector: ImageVector,
    onClick: () -> Unit,
) {
    Box(modifier = modifier.clickable(onClick = onClick)) {
        Image(
            imageVector = imageVector,
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(10.dp)),
            contentDescription = "",
        )
    }
}
