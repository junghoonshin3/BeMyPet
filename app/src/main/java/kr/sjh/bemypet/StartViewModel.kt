package kr.sjh.bemypet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kr.sjh.data.repository.SettingRepository
import kr.sjh.data.session.SessionStore
import javax.inject.Inject

@HiltViewModel
class StartViewModel @Inject constructor(
    private val sessionStore: SessionStore,
    private val settingRepository: SettingRepository,
) : ViewModel() {

    val isDarkTheme = settingRepository.getDarkTheme()
    val hasSeenOnboarding = settingRepository.getHasSeenOnboarding()
    val session = sessionStore.session

    fun updateIsDarkTheme(isDarkTheme: Boolean) = viewModelScope.launch {
        settingRepository.updateIsDarkTheme(isDarkTheme)
    }

    fun completeOnboarding() = viewModelScope.launch {
        settingRepository.updateHasSeenOnboarding(true)
    }
}
