package kr.sjh.setting.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
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
    private var lastLoadedUserId: String? = null
    private var loadProfileJob: Job? = null

    fun signOut() {
        viewModelScope.launch {
            clearProfileCache()
            authRepository.signOut()
        }
    }

    fun deleteAccount(userId: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        viewModelScope.launch {
            authRepository.deleteAccount(userId, onSuccess, onFailure)
        }
    }

    fun loadProfile(userId: String, force: Boolean = false) {
        if (userId.isBlank()) return

        if (!force) {
            val hasCachedProfile = _profileUiState.value.profile != null
            if (lastLoadedUserId == userId && hasCachedProfile) return
            if (loadProfileJob?.isActive == true) return
        }

        loadProfileJob?.cancel()
        loadProfileJob = viewModelScope.launch {
            _profileUiState.update { it.copy(loading = true) }
            runCatching { authRepository.getProfile(userId) }
                .onSuccess { profile ->
                    lastLoadedUserId = userId
                    _profileUiState.update { it.copy(profile = profile) }
                }
                .onFailure {
                    // Keep previous profile on transient failure to avoid UI flicker.
                }
            _profileUiState.update {
                it.copy(loading = false)
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
                    loadProfile(userId = userId, force = true)
                    onSuccess()
                },
                onFailure = onFailure
            )
        }
    }

    private fun clearProfileCache() {
        lastLoadedUserId = null
        loadProfileJob?.cancel()
        loadProfileJob = null
        _profileUiState.value = ProfileUiState()
    }

}
