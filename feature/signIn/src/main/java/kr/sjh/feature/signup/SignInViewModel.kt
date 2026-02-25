package kr.sjh.feature.signup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kr.sjh.data.repository.AuthRepository
import javax.inject.Inject

data class SignInUiState(
    val loadingProvider: LoadingProvider? = null,
    val isSignedIn: Boolean = false,
    val errorMessage: String? = null
) {
    val isLoading: Boolean
        get() = loadingProvider != null
}

enum class LoadingProvider {
    Google,
    Kakao,
}

@HiltViewModel
class SignInViewModel @Inject constructor(private val authRepository: AuthRepository) :
    ViewModel() {

    private val _uiState = MutableStateFlow(SignInUiState())
    val uiState = _uiState.asStateFlow()

    fun onGoogleSignIn(idToken: String, nonce: String) {
        viewModelScope.launch { signInWithGoogle(idToken, nonce) }
    }

    fun onKakaoSignIn() {
        viewModelScope.launch { signInWithKakao() }
    }

    suspend fun signOut() {
        authRepository.signOut()
        _uiState.value = SignInUiState()
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    private suspend fun signInWithGoogle(idToken: String, nonce: String) {
        _uiState.value = _uiState.value.copy(
            loadingProvider = LoadingProvider.Google,
            errorMessage = null
        )
        authRepository.signInWithGoogle(idToken, nonce, {
            _uiState.value =
                _uiState.value.copy(
                    isSignedIn = true,
                    loadingProvider = null,
                    errorMessage = null
                )
        }, { e ->
            val safeMessage = mapSignInFailureMessage(e, provider = LoginProvider.Google)
            _uiState.value =
                _uiState.value.copy(
                    isSignedIn = false,
                    loadingProvider = null,
                    errorMessage = safeMessage
                )
        })
    }

    private suspend fun signInWithKakao() {
        _uiState.value = _uiState.value.copy(
            loadingProvider = LoadingProvider.Kakao,
            errorMessage = null
        )
        authRepository.signInWithKakao(onSuccess = {
            _uiState.value =
                _uiState.value.copy(
                    isSignedIn = true,
                    loadingProvider = null,
                    errorMessage = null
                )
        }, onFailure = { e ->
            val safeMessage = mapSignInFailureMessage(e, provider = LoginProvider.Kakao)
            _uiState.value =
                _uiState.value.copy(
                    isSignedIn = false,
                    loadingProvider = null,
                    errorMessage = safeMessage
                )
        })
    }

    private fun mapSignInFailureMessage(exception: Exception, provider: LoginProvider): String {
        val raw = exception.message?.trim().orEmpty()
        val lowered = raw.lowercase()

        return when {
            provider == LoginProvider.Google && (
                lowered.contains("developer_error") ||
                    lowered.contains("invalid_audience") ||
                    (lowered.contains("audience") && lowered.contains("client")) ||
                    lowered.contains("invalid login credentials")
                ) -> {
                "Google 로그인 설정이 맞지 않아 관리자 확인이 필요해요."
            }

            provider == LoginProvider.Kakao && (
                (lowered.contains("email") && lowered.contains("confirm")) ||
                    (lowered.contains("email") && lowered.contains("required"))
                ) -> {
                "카카오 계정의 이메일 확인이 필요해요. 카카오 계정 설정을 확인해 주세요."
            }

            provider == LoginProvider.Kakao && (
                lowered.contains("provider") && lowered.contains("enabled")
                ) -> {
                "카카오 로그인 설정이 맞지 않아 관리자 확인이 필요해요."
            }

            raw.isNotBlank() -> raw
            provider == LoginProvider.Kakao -> "카카오 로그인에 실패했어요. 잠시 후 다시 시도해 주세요."
            else -> "Google 로그인에 실패했어요. 잠시 후 다시 시도해 주세요."
        }
    }

    private enum class LoginProvider {
        Google,
        Kakao
    }
}
