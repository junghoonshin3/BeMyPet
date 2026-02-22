package kr.sjh.setting.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kr.sjh.core.model.UserProfile
import kr.sjh.data.repository.AuthRepository
import javax.inject.Inject

data class ProfileUiState(
    val loading: Boolean = false,
    val profile: UserProfile? = null
)

@HiltViewModel
class SettingViewModel @Inject constructor(private val authRepository: AuthRepository) :
    ViewModel() {

    private val _profileUiState = MutableStateFlow(ProfileUiState())
    val profileUiState = _profileUiState.asStateFlow()

    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
        }
    }

    fun deleteAccount(userId: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        viewModelScope.launch {
            authRepository.deleteAccount(userId, onSuccess, onFailure)
        }
    }

    fun loadProfile(userId: String) {
        viewModelScope.launch {
            _profileUiState.update { it.copy(loading = true) }
            val profile = authRepository.getProfile(userId)
            _profileUiState.update {
                it.copy(loading = false, profile = profile)
            }
        }
    }

    fun updateProfile(
        userId: String,
        displayName: String,
        avatarUrl: String?,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        viewModelScope.launch {
            authRepository.updateProfile(
                userId = userId,
                displayName = displayName,
                avatarUrl = avatarUrl,
                onSuccess = {
                    loadProfile(userId)
                    onSuccess()
                },
                onFailure = onFailure
            )
        }
    }

}
