package kr.sjh.feature.signup

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kr.sjh.core.model.SessionState
import kr.sjh.core.model.notification.UserInterestProfile
import kr.sjh.data.repository.NotificationRepository
import kr.sjh.data.repository.SettingRepository
import org.junit.Rule
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class OnboardingViewModelTest {

    @get:Rule
    private val mainDispatcherRule = MainDispatcherRule()

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

    @Test
    fun `submit with permission denied stores push opt out`() = runTest {
        val fakeSettingRepository = FakeSettingRepository()
        val viewModel = OnboardingViewModel(
            notificationRepository = FakeNotificationRepository(),
            settingRepository = fakeSettingRepository,
        )
        viewModel.setPushOptIn(true)

        viewModel.submit(
            session = SessionState.NoAuthenticated(isSignOut = false),
            resolvedPushOptIn = false,
        )
        advanceUntilIdle()

        assertFalse(fakeSettingRepository.lastPushOptIn)
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

    override suspend fun getInterestProfile(userId: String): UserInterestProfile? = null

    override suspend fun upsertSubscription(
        userId: String,
        token: String,
        pushOptIn: Boolean,
        timezone: String,
    ) = Unit

    override suspend fun touchLastActive(userId: String) = Unit
}

private class FakeSettingRepository : SettingRepository {
    var lastPushOptIn: Boolean = true

    override fun getDarkTheme(): Flow<Boolean> = flowOf(false)

    override fun getHasSeenOnboarding(): Flow<Boolean> = flowOf(false)

    override fun getPushOptIn(): Flow<Boolean> = flowOf(true)

    override suspend fun updateIsDarkTheme(isDarkTheme: Boolean) = Unit

    override suspend fun updateHasSeenOnboarding(seen: Boolean) = Unit

    override suspend fun updatePushOptIn(enabled: Boolean) {
        lastPushOptIn = enabled
    }
}

private class MainDispatcherRule(
    private val dispatcher: TestDispatcher = UnconfinedTestDispatcher(),
) : TestWatcher() {
    override fun starting(description: Description) {
        Dispatchers.setMain(dispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}
