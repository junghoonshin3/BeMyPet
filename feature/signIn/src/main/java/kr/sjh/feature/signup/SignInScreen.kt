package kr.sjh.feature.signup

import android.util.Log
import android.util.Patterns
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import kr.sjh.core.common.credential.AccountManager
import kr.sjh.core.common.credential.SignInResult
import kr.sjh.core.designsystem.R


@Composable
fun SignInRoute(
    viewModel: SignInViewModel = hiltViewModel(),
    accountManager: AccountManager,
    onSignInSuccess: () -> Unit
) {
    val focusManager = LocalFocusManager.current

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.isSignedIn) {
        if (uiState.isSignedIn) {
            onSignInSuccess()
        }
    }

    SignInScreen(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primary)
            .pointerInput(Unit) {
                detectTapGestures(onTap = { focusManager.clearFocus() }) // 터치 감지해서 포커스 해제
            },
        uiState = uiState,
        accountManager = accountManager,
        onSignIn = viewModel::onSignIn,
    )
}

@Composable
private fun SignInScreen(
    modifier: Modifier = Modifier,
    uiState: SignInUiState,
    onSignIn: (SignUpModel) -> Unit,
    accountManager: AccountManager,
) {


    Column(
        modifier = modifier
    ) {
        val googleLoginImage = ImageVector.vectorResource(R.drawable.android_light_rd_ctn)
        val coroutineScope = rememberCoroutineScope()
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .padding(10.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                style = MaterialTheme.typography.headlineLarge.copy(
                    color = MaterialTheme.colorScheme.onPrimary, fontSize = 30.sp
                ), text = stringResource(R.string.app_name)
            )
        }
        Column(
            Modifier
                .fillMaxWidth()
                .height(300.dp)
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                modifier = Modifier.size(80.dp),
                imageVector = ImageVector.vectorResource(R.drawable.animal_carnivore_cartoon_3_svgrepo_com),
                contentDescription = "dog"
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(color = Color.Blue)) {
                        append("간편 로그인")
                    }
                    append("으로 ")
                    withStyle(SpanStyle(color = Color.Red)) {
                        append("3초")
                    }
                    append("만에 가입해보세요!")
                }, style = MaterialTheme.typography.headlineSmall.copy(fontSize = 20.sp)
            )
        }
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            SocialLoginButton(modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
                .height(60.dp),
                imageVector = googleLoginImage,
                onClick = {
                    coroutineScope.launch {
                        when (val result = accountManager.signIn(false)) {
                            SignInResult.Cancelled -> {}
                            is SignInResult.Failure -> {}
                            SignInResult.NoCredentials -> {
                                // 인증 관리자 등록된 계정이 없는 경우
                                accountManager.signIn(false)
                            }

                            is SignInResult.Success -> {
                                onSignIn(
                                    SignUpModel(
                                        result.idToken, result.nonce, "google"
                                    )
                                )
                            }
                        }
                    }
                })
        }
    }
}

@Composable
private fun SocialLoginButton(
    modifier: Modifier = Modifier,
    imageVector: ImageVector,
    onClick: () -> Unit,
) {
    Image(
        imageVector = imageVector,
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick),
        contentDescription = "",
    )
}

@Composable
fun EmailInputField(
    email: String, onEmailChange: (String) -> Unit, modifier: Modifier = Modifier
) {
    var isValidEmail = remember(email) { Patterns.EMAIL_ADDRESS.matcher(email).matches() }

    OutlinedTextField(value = email, onValueChange = onEmailChange, label = {
        Text(
            "이메일 주소"
        )
    }, placeholder = {
        Text(
            text = "example@domain.com"
        )
    }, isError = !isValidEmail, // 이메일 형식이 잘못된 경우 에러 표시
        keyboardOptions = KeyboardOptions.Default.copy(
            keyboardType = KeyboardType.Email
        ), singleLine = true, modifier = modifier, colors = TextFieldDefaults.colors(
            selectionColors = TextSelectionColors(
                MaterialTheme.colorScheme.onPrimary, MaterialTheme.colorScheme.onPrimary
            ),
            errorContainerColor = MaterialTheme.colorScheme.primary,
            focusedContainerColor = MaterialTheme.colorScheme.primary,
            unfocusedContainerColor = MaterialTheme.colorScheme.primary,
            focusedIndicatorColor = MaterialTheme.colorScheme.onPrimary,
            unfocusedIndicatorColor = MaterialTheme.colorScheme.onPrimary,
            unfocusedLabelColor = MaterialTheme.colorScheme.onPrimary,
            focusedLabelColor = MaterialTheme.colorScheme.onPrimary,
            cursorColor = MaterialTheme.colorScheme.onPrimary,

            ), trailingIcon = {
            if (!isValidEmail) {
                IconButton(onClick = {
                    onEmailChange("")
                }) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Invalid Email",
                        tint = Color.Red
                    )
                }
            }
        })
}

@Composable
fun PasswordInputField(
    password: String, onPasswordChange: (String) -> Unit, modifier: Modifier = Modifier
) {
    var passwordVisible by remember { mutableStateOf(false) } // 비밀번호 보이기 상태

    OutlinedTextField(value = password,
        onValueChange = onPasswordChange,
        label = { Text("비밀번호") },
        placeholder = { Text("비밀번호 입력") },
        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions.Default.copy(
            keyboardType = KeyboardType.Password
        ),
        singleLine = true,
        modifier = modifier,
        colors = TextFieldDefaults.colors(
            selectionColors = TextSelectionColors(
                MaterialTheme.colorScheme.onPrimary, MaterialTheme.colorScheme.onPrimary
            ),
            errorContainerColor = MaterialTheme.colorScheme.primary,
            focusedContainerColor = MaterialTheme.colorScheme.primary,
            unfocusedContainerColor = MaterialTheme.colorScheme.primary,
            focusedIndicatorColor = MaterialTheme.colorScheme.onPrimary,
            unfocusedIndicatorColor = MaterialTheme.colorScheme.onPrimary,
            unfocusedLabelColor = MaterialTheme.colorScheme.onPrimary,
            focusedLabelColor = MaterialTheme.colorScheme.onPrimary,
            cursorColor = MaterialTheme.colorScheme.onPrimary,
        ),
        trailingIcon = {
            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                Icon(
                    imageVector = if (passwordVisible) ImageVector.vectorResource(R.drawable.passworkd_show) else ImageVector.vectorResource(
                        R.drawable.password_hide
                    ),
                    contentDescription = if (passwordVisible) "Hide password" else "Show password"
                )
            }
        })
}