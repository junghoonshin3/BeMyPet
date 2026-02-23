package kr.sjh.feature.signup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kr.sjh.core.model.SessionState
import kr.sjh.data.repository.NotificationRepository
import kr.sjh.data.repository.SettingRepository
import javax.inject.Inject

data class OnboardingSubmitPayload(
    val regions: List<String>,
    val species: List<String>,
    val pushOptIn: Boolean,
)

data class OnboardingPreferenceUiState(
    val regions: Set<String> = emptySet(),
    val species: Set<String> = emptySet(),
    val pushOptIn: Boolean = true,
)

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val notificationRepository: NotificationRepository,
    private val settingRepository: SettingRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingPreferenceUiState())
    val uiState = _uiState.asStateFlow()

    fun toggleRegion(code: String) {
        val normalized = code.trim()
        if (normalized.isBlank()) return

        _uiState.update { current ->
            val next = current.regions.toMutableSet()
            if (!next.add(normalized)) {
                next.remove(normalized)
            }
            current.copy(regions = next)
        }
    }

    fun toggleSpecies(code: String) {
        val normalized = code.trim()
        if (normalized.isBlank()) return

        _uiState.update { current ->
            val next = current.species.toMutableSet()
            if (!next.add(normalized)) {
                next.remove(normalized)
            }
            current.copy(species = next)
        }
    }

    fun setPushOptIn(enabled: Boolean) {
        _uiState.update { it.copy(pushOptIn = enabled) }
    }

    fun submit(session: SessionState, resolvedPushOptIn: Boolean? = null) {
        val payload = buildSubmitPayloadForTest().let {
            if (resolvedPushOptIn == null) {
                it
            } else {
                it.copy(pushOptIn = resolvedPushOptIn)
            }
        }

        viewModelScope.launch {
            settingRepository.updatePushOptIn(payload.pushOptIn)
            val userId = (session as? SessionState.Authenticated)?.user?.id.orEmpty()
            if (userId.isBlank()) return@launch

            notificationRepository.upsertInterestProfile(
                userId = userId,
                regions = payload.regions,
                species = payload.species,
                sexes = emptyList(),
                sizes = emptyList(),
                pushEnabled = payload.pushOptIn,
            )
        }
    }

    fun buildSubmitPayloadForTest(): OnboardingSubmitPayload =
        OnboardingSubmitPayload(
            regions = uiState.value.regions.sorted(),
            species = uiState.value.species.sorted(),
            pushOptIn = uiState.value.pushOptIn,
        )
}
