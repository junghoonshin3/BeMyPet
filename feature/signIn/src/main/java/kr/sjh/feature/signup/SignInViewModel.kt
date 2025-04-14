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
            }
        }
    }

    private suspend fun signInWithGoogle(idToken: String, nonce: String) {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = "")
        authRepository.signInWithGoogle(idToken, nonce, {
            _uiState.value =
                _uiState.value.copy(isSignedIn = true, isLoading = false, errorMessage = "")
        }, { e ->
            _uiState.value =
                _uiState.value.copy(isSignedIn = false, isLoading = false, errorMessage = e.message)
        })
    }
}