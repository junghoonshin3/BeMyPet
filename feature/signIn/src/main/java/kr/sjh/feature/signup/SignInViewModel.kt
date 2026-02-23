package kr.sjh.feature.signup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kr.sjh.data.repository.AuthRepository
import javax.inject.Inject

data class SignUpModel(
    val idToken: String, val nonce: String, val type: String = "google"
)

data class SignInUiState(
    val isLoading: Boolean = false,
    val isSignedIn: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class SignInViewModel @Inject constructor(private val authRepository: AuthRepository) :
    ViewModel() {

    private val _uiState = MutableStateFlow(SignInUiState())
    val uiState = _uiState.asStateFlow()

    fun onSignIn(
        loginState: SignUpModel
    ) {
        val (idToken, nonce, type) = loginState
        viewModelScope.launch {
            if (type == "google") {
                signInWithGoogle(idToken, nonce)
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isSignedIn = false,
                    errorMessage = "이메일 로그인은 곧 지원 예정이에요."
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    private suspend fun signInWithGoogle(idToken: String, nonce: String) {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = "")
        authRepository.signInWithGoogle(idToken, nonce, {
            _uiState.value =
                _uiState.value.copy(isSignedIn = true, isLoading = false, errorMessage = "")
        }, { e ->
            val safeMessage = mapSignInFailureMessage(e)
            _uiState.value =
                _uiState.value.copy(
                    isSignedIn = false,
                    isLoading = false,
                    errorMessage = safeMessage
                )
        })
    }

    private fun mapSignInFailureMessage(exception: Exception): String {
        val raw = exception.message?.trim().orEmpty()
        val lowered = raw.lowercase()

        return when {
            lowered.contains("developer_error") ||
                lowered.contains("invalid_audience") ||
                (lowered.contains("audience") && lowered.contains("client")) ||
                lowered.contains("invalid login credentials") -> {
                "Google 로그인 설정이 맞지 않아 관리자 확인이 필요해요."
            }

            raw.isNotBlank() -> raw
            else -> "Google 로그인에 실패했어요. 잠시 후 다시 시도해 주세요."
        }
    }
}
