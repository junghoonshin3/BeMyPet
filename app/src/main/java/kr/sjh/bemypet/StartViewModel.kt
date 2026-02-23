package kr.sjh.bemypet

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kr.sjh.data.notification.InterestProfileSyncCoordinator
import kr.sjh.core.model.SessionState
import kr.sjh.data.repository.NotificationRepository
import kr.sjh.data.repository.SettingRepository
import kr.sjh.data.session.SessionStore
import java.util.TimeZone
import javax.inject.Inject

data class PushSyncState(
    val session: SessionState,
    val hasSeenOnboarding: Boolean,
    val pushOptIn: Boolean,
)

@HiltViewModel
class StartViewModel @Inject constructor(
    private val sessionStore: SessionStore,
    private val settingRepository: SettingRepository,
    private val notificationRepository: NotificationRepository,
    private val interestProfileSyncCoordinator: InterestProfileSyncCoordinator,
) : ViewModel() {
    private companion object {
        const val TAG = "StartViewModel"
    }

    private var lastFavoriteInterestSyncUserId: String? = null

    val isDarkTheme = settingRepository.getDarkTheme()
    val hasSeenOnboarding = settingRepository.getHasSeenOnboarding()
    val pushOptIn = settingRepository.getPushOptIn()
    val session = sessionStore.session
    val pushSyncState = combine(
        session,
        hasSeenOnboarding,
        pushOptIn,
    ) { sessionState, seenOnboarding, pushEnabled ->
        PushSyncState(
            session = sessionState,
            hasSeenOnboarding = seenOnboarding,
            pushOptIn = pushEnabled,
        )
    }.distinctUntilChanged()

    fun updateIsDarkTheme(isDarkTheme: Boolean) = viewModelScope.launch {
        settingRepository.updateIsDarkTheme(isDarkTheme)
    }

    fun completeOnboarding() = viewModelScope.launch {
        settingRepository.updateHasSeenOnboarding(true)
    }

    fun syncPushSubscription(userId: String, token: String, pushOptIn: Boolean) = viewModelScope.launch {
        val normalizedUserId = userId.trim()
        val normalizedToken = token.trim()
        if (normalizedUserId.isBlank() || normalizedToken.isBlank()) return@launch

        runCatching {
            notificationRepository.upsertSubscription(
                userId = normalizedUserId,
                token = normalizedToken,
                pushOptIn = pushOptIn,
                timezone = TimeZone.getDefault().id.ifBlank { "Asia/Seoul" },
            )
        }.onFailure { throwable ->
            Log.e(TAG, "Failed to sync push subscription", throwable)
        }

        runCatching {
            notificationRepository.upsertInterestPushEnabled(
                userId = normalizedUserId,
                pushEnabled = pushOptIn,
            )
        }.onFailure { throwable ->
            Log.e(TAG, "Failed to sync interest push_enabled", throwable)
        }
    }

    fun syncInterestProfileFromFavoritesOnce(userId: String) = viewModelScope.launch {
        val normalizedUserId = userId.trim()
        if (normalizedUserId.isBlank()) return@launch
        if (lastFavoriteInterestSyncUserId == normalizedUserId) return@launch

        runCatching {
            interestProfileSyncCoordinator.syncFromFavorites(normalizedUserId)
            lastFavoriteInterestSyncUserId = normalizedUserId
        }.onFailure { throwable ->
            Log.w(TAG, "Failed to sync favorite-derived interest profile", throwable)
        }
    }

    fun clearFavoriteInterestSyncUser() {
        lastFavoriteInterestSyncUserId = null
    }

    fun touchLastActive(userId: String) = viewModelScope.launch {
        val normalizedUserId = userId.trim()
        if (normalizedUserId.isBlank()) return@launch
        runCatching {
            notificationRepository.touchLastActive(normalizedUserId)
        }.onFailure { throwable ->
            Log.e(TAG, "Failed to touch last active", throwable)
        }
    }
}
