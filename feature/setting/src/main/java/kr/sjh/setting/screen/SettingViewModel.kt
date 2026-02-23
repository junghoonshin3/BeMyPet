package kr.sjh.setting.screen

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kr.sjh.core.model.UserProfile
import kr.sjh.core.model.setting.SettingType
import kr.sjh.data.repository.AuthRepository
import kr.sjh.data.repository.SettingRepository
import javax.inject.Inject

sealed interface SettingUiEvent {
    data class ShowMessage(val message: String) : SettingUiEvent
}

data class ProfileEditDraftState(
    val isVisible: Boolean = false,
    val editingUserId: String? = null,
    val nameInput: String = "",
    val originalAvatarUrlForSave: String? = null,
    val selectedAvatarUri: String? = null,
    val selectedAvatarBytes: ByteArray? = null,
) {
    val avatarPreviewModel: String?
        get() = selectedAvatarUri ?: originalAvatarUrlForSave
    val hasNewAvatarSelection: Boolean
        get() = selectedAvatarUri != null
}

data class ProfileUiState(
    val loading: Boolean = false,
    val profile: UserProfile? = null,
    val selectedTheme: SettingType = SettingType.LIGHT_THEME,
    val pushOptIn: Boolean = true,
    val isDeleteUserDialogVisible: Boolean = false,
    val profileEditDraft: ProfileEditDraftState = ProfileEditDraftState(),
)

@HiltViewModel
class SettingViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val settingRepository: SettingRepository,
) :
    ViewModel() {

    private val _profileUiState = MutableStateFlow(ProfileUiState())
    val profileUiState = _profileUiState.asStateFlow()
    private val _uiEvent = MutableSharedFlow<SettingUiEvent>(extraBufferCapacity = 1)
    val uiEvent = _uiEvent.asSharedFlow()

    init {
        observePushOptIn()
    }

    fun syncTheme(isDarkTheme: Boolean) {
        val selectedTheme = if (isDarkTheme) SettingType.DARK_THEME else SettingType.LIGHT_THEME
        _profileUiState.update { state ->
            if (state.selectedTheme == selectedTheme) {
                state
            } else {
                state.copy(selectedTheme = selectedTheme)
            }
        }
    }

    fun selectTheme(selectedTheme: SettingType) {
        _profileUiState.update { it.copy(selectedTheme = selectedTheme) }
    }

    fun setPushOptIn(enabled: Boolean) {
        _profileUiState.update { state ->
            if (state.pushOptIn == enabled) {
                state
            } else {
                state.copy(pushOptIn = enabled)
            }
        }
        viewModelScope.launch {
            settingRepository.updatePushOptIn(enabled)
        }
    }

    fun showDeleteUserDialog() {
        _profileUiState.update { it.copy(isDeleteUserDialogVisible = true) }
    }

    fun hideDeleteUserDialog() {
        _profileUiState.update { it.copy(isDeleteUserDialogVisible = false) }
    }

    fun startProfileEdit(userId: String, displayName: String, currentAvatarUrl: String?) {
        _profileUiState.update {
            it.copy(
                profileEditDraft = ProfileEditDraftState(
                    isVisible = true,
                    editingUserId = userId,
                    nameInput = displayName,
                    originalAvatarUrlForSave = currentAvatarUrl,
                )
            )
        }
    }

    fun setProfileEditVisible(isVisible: Boolean) {
        _profileUiState.update { state ->
            state.copy(profileEditDraft = state.profileEditDraft.copy(isVisible = isVisible))
        }
    }

    fun reopenProfileEditDialogIfNeeded() {
        _profileUiState.update { state ->
            if (state.profileEditDraft.editingUserId == null) {
                state
            } else {
                state.copy(profileEditDraft = state.profileEditDraft.copy(isVisible = true))
            }
        }
    }

    fun dismissProfileEdit(clearDraft: Boolean) {
        _profileUiState.update { state ->
            if (clearDraft) {
                state.copy(profileEditDraft = ProfileEditDraftState())
            } else {
                state.copy(profileEditDraft = state.profileEditDraft.copy(isVisible = false))
            }
        }
    }

    fun updateProfileEditNameInput(nameInput: String) {
        _profileUiState.update { state ->
            state.copy(profileEditDraft = state.profileEditDraft.copy(nameInput = nameInput))
        }
    }

    fun setPickedAvatar(uri: Uri, bytes: ByteArray?) {
        _profileUiState.update { state ->
            state.copy(
                profileEditDraft = state.profileEditDraft.copy(
                    isVisible = true,
                    selectedAvatarUri = uri.toString(),
                    selectedAvatarBytes = bytes,
                )
            )
        }
    }

    fun clearTransientUiState() {
        _profileUiState.update {
            it.copy(
                isDeleteUserDialogVisible = false,
                profileEditDraft = ProfileEditDraftState()
            )
        }
    }

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

    fun updateProfileWithAvatar(
        userId: String,
        displayName: String,
        avatarBytes: ByteArray?,
        currentAvatarUrl: String?,
    ) {
        viewModelScope.launch {
            val avatarUrl = runCatching {
                if (avatarBytes != null) {
                    authRepository.uploadProfileAvatar(
                        userId = userId,
                        bytes = avatarBytes,
                        contentType = "image/jpeg",
                    )
                } else {
                    currentAvatarUrl ?: profileUiState.value.profile?.avatarUrl
                }
            }.getOrElse { throwable ->
                emitMessage(throwable.message ?: PROFILE_UPDATE_FAILED_MESSAGE)
                return@launch
            }

            authRepository.updateProfile(
                userId = userId,
                displayName = displayName,
                avatarUrl = avatarUrl,
                onSuccess = {
                    loadProfile(userId)
                    viewModelScope.launch {
                        emitMessage(PROFILE_UPDATED_MESSAGE)
                    }
                },
                onFailure = { exception ->
                    viewModelScope.launch {
                        emitMessage(exception.message ?: PROFILE_UPDATE_FAILED_MESSAGE)
                    }
                }
            )
        }
    }

    private suspend fun emitMessage(message: String) {
        _uiEvent.emit(SettingUiEvent.ShowMessage(message))
    }

    private fun observePushOptIn() {
        viewModelScope.launch {
            settingRepository.getPushOptIn().collectLatest { enabled ->
                _profileUiState.update { state ->
                    if (state.pushOptIn == enabled) {
                        state
                    } else {
                        state.copy(pushOptIn = enabled)
                    }
                }
            }
        }
    }

    companion object {
        private const val PROFILE_UPDATED_MESSAGE = "프로필을 업데이트했어요."
        private const val PROFILE_UPDATE_FAILED_MESSAGE = "프로필 업데이트에 실패했어요."
    }
}
