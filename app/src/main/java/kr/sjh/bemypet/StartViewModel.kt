package kr.sjh.bemypet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kr.sjh.data.repository.NotificationRepository
import kr.sjh.data.repository.SettingRepository
import kr.sjh.data.session.SessionStore
import java.util.TimeZone
import javax.inject.Inject

@HiltViewModel
class StartViewModel @Inject constructor(
    private val sessionStore: SessionStore,
    private val settingRepository: SettingRepository,
    private val notificationRepository: NotificationRepository,
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

    fun syncPushSubscription(userId: String, token: String) = viewModelScope.launch {
        val normalizedUserId = userId.trim()
        val normalizedToken = token.trim()
        if (normalizedUserId.isBlank() || normalizedToken.isBlank()) return@launch

        val pushOptIn = settingRepository.getPushOptIn().first()
        notificationRepository.upsertSubscription(
            userId = normalizedUserId,
            token = normalizedToken,
            pushOptIn = pushOptIn,
            timezone = TimeZone.getDefault().id.ifBlank { "Asia/Seoul" },
        )
    }

    fun touchLastActive(userId: String) = viewModelScope.launch {
        val normalizedUserId = userId.trim()
        if (normalizedUserId.isBlank()) return@launch
        notificationRepository.touchLastActive(normalizedUserId)
    }
}
