package kr.sjh.feature.signup

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kr.sjh.data.repository.NotificationRepository
import kr.sjh.data.repository.SettingRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class OnboardingViewModelTest {

    @Test
    fun `complete onboarding emits selected interests and push opt in`() {
        val viewModel = OnboardingViewModel(
            notificationRepository = FakeNotificationRepository(),
            settingRepository = FakeSettingRepository(),
        )

        viewModel.toggleSpecies("dog")
        viewModel.toggleRegion("6110000")
        viewModel.setPushOptIn(true)

        val payload = viewModel.buildSubmitPayloadForTest()

        assertEquals(listOf("dog"), payload.species)
        assertEquals(listOf("6110000"), payload.regions)
        assertTrue(payload.pushOptIn)
    }
}

private class FakeNotificationRepository : NotificationRepository {
    override suspend fun upsertInterestProfile(
        userId: String,
        regions: List<String>,
        species: List<String>,
        sexes: List<String>,
        sizes: List<String>,
        pushEnabled: Boolean,
    ) = Unit

    override suspend fun upsertSubscription(
        userId: String,
        token: String,
        pushOptIn: Boolean,
        timezone: String,
    ) = Unit

    override suspend fun touchLastActive(userId: String) = Unit
}

private class FakeSettingRepository : SettingRepository {
    override fun getDarkTheme(): Flow<Boolean> = flowOf(false)

    override fun getHasSeenOnboarding(): Flow<Boolean> = flowOf(false)

    override fun getPushOptIn(): Flow<Boolean> = flowOf(true)

    override suspend fun updateIsDarkTheme(isDarkTheme: Boolean) = Unit

    override suspend fun updateHasSeenOnboarding(seen: Boolean) = Unit

    override suspend fun updatePushOptIn(enabled: Boolean) = Unit
}
