package kr.sjh.bemypet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kr.sjh.core.model.SessionState
import kr.sjh.data.repository.AuthRepository
import kr.sjh.data.repository.SettingRepository
import javax.inject.Inject

@HiltViewModel
class StartViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val settingRepository: SettingRepository,
) : ViewModel() {

    val isDarkTheme = settingRepository.getDarkTheme()
    val hasSeenOnboarding = settingRepository.getHasSeenOnboarding()

    val session = authRepository.getSessionFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SessionState.Initializing)

    fun updateIsDarkTheme(isDarkTheme: Boolean) = viewModelScope.launch {
        settingRepository.updateIsDarkTheme(isDarkTheme)
    }

    fun completeOnboarding() = viewModelScope.launch {
        settingRepository.updateHasSeenOnboarding(true)
    }
}
