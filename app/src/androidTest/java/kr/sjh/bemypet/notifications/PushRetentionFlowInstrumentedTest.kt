package kr.sjh.bemypet.notifications

import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kr.sjh.bemypet.StartViewModel
import kr.sjh.core.model.SessionState
import kr.sjh.core.model.User
import kr.sjh.core.model.UserProfile
import kr.sjh.core.model.notification.UserInterestProfile
import kr.sjh.data.notification.InterestProfileSyncCoordinator
import kr.sjh.data.repository.AuthRepository
import kr.sjh.data.repository.FavouriteRepository
import kr.sjh.data.repository.NotificationRepository
import kr.sjh.data.repository.SettingRepository
import kr.sjh.data.session.SessionStore
import kr.sjh.feature.signup.OnboardingViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class PushRetentionFlowInstrumentedTest {

    @Test
    fun onboardingTokenSyncAndPushParse_endToEndFlow() {
        val userId = "user-123"
        val token = "fcm-token-abc"
        val fakeNotificationRepository = FakeNotificationRepository()
        val fakeSettingRepository = FakeSettingRepository(initialPushOptIn = true)
        val session = SessionState.Authenticated(
            user = User(id = userId, bannedUntil = null)
        )

        val onboardingViewModel = OnboardingViewModel(
            notificationRepository = fakeNotificationRepository,
            settingRepository = fakeSettingRepository,
        )

        onboardingViewModel.toggleRegion("6110000")
        onboardingViewModel.toggleSpecies("dog")
        onboardingViewModel.submit(session)

        val interest = fakeNotificationRepository.awaitInterestProfileUpsert()
        assertEquals(userId, interest.userId)
        assertEquals(listOf("6110000"), interest.regions)
        assertEquals(listOf("dog"), interest.species)
        assertTrue(interest.pushEnabled)

        val startViewModel = StartViewModel(
            sessionStore = SessionStore(FakeAuthRepository(session)),
            settingRepository = fakeSettingRepository,
            notificationRepository = fakeNotificationRepository,
            interestProfileSyncCoordinator = InterestProfileSyncCoordinator(
                favouriteRepository = FakeFavouriteRepository(),
                notificationRepository = fakeNotificationRepository,
                settingRepository = fakeSettingRepository,
            ),
        )
        startViewModel.syncPushSubscription(userId = userId, token = token, pushOptIn = false)

        val subscription = fakeNotificationRepository.awaitSubscriptionUpsert()
        assertEquals(userId, subscription.userId)
        assertEquals(token, subscription.token)
        assertFalse(subscription.pushOptIn)
        assertTrue(subscription.timezone.isNotBlank())

        val interestPushEnabled = fakeNotificationRepository.awaitInterestPushEnabledUpsert()
        assertEquals(userId, interestPushEnabled.userId)
        assertFalse(interestPushEnabled.pushEnabled)

        val parsed = PushPayloadParser.parse(
            mapOf(
                "notice_no" to "N-100",
                "campaign_type" to "new_animal",
            )
        )
        assertEquals("N-100", parsed.noticeNo)
        assertEquals("new_animal", parsed.campaignType)
    }
}

private class FakeFavouriteRepository : FavouriteRepository {
    override fun isExist(desertionNo: String): Boolean = false

    override suspend fun addPet(pet: kr.sjh.core.model.adoption.Pet) = Unit

    override suspend fun removePet(desertionNo: String) = Unit

    override fun getFavouritePets(): Flow<List<kr.sjh.core.model.adoption.Pet>> = flowOf(emptyList())

    override suspend fun backfillFavouriteImagesIfNeeded() = Unit
}

private data class InterestUpsertCall(
    val userId: String,
    val regions: List<String>,
    val species: List<String>,
    val pushEnabled: Boolean,
)

private data class SubscriptionUpsertCall(
    val userId: String,
    val token: String,
    val pushOptIn: Boolean,
    val timezone: String,
)

private data class InterestPushEnabledUpsertCall(
    val userId: String,
    val pushEnabled: Boolean,
)

private class FakeNotificationRepository : NotificationRepository {
    private val interestLatch = CountDownLatch(1)
    private val subscriptionLatch = CountDownLatch(1)
    private val interestPushEnabledLatch = CountDownLatch(1)

    @Volatile
    private var interestUpsert: InterestUpsertCall? = null

    @Volatile
    private var subscriptionUpsert: SubscriptionUpsertCall? = null

    @Volatile
    private var interestPushEnabledUpsert: InterestPushEnabledUpsertCall? = null

    override suspend fun upsertInterestProfile(
        userId: String,
        regions: List<String>,
        species: List<String>,
        sexes: List<String>,
        sizes: List<String>,
        pushEnabled: Boolean,
    ) {
        interestUpsert = InterestUpsertCall(
            userId = userId,
            regions = regions,
            species = species,
            pushEnabled = pushEnabled,
        )
        interestLatch.countDown()
    }

    override suspend fun upsertSubscription(
        userId: String,
        token: String,
        pushOptIn: Boolean,
        timezone: String,
    ) {
        subscriptionUpsert = SubscriptionUpsertCall(
            userId = userId,
            token = token,
            pushOptIn = pushOptIn,
            timezone = timezone,
        )
        subscriptionLatch.countDown()
    }

    override suspend fun getInterestProfile(userId: String): UserInterestProfile? = null

    override suspend fun upsertInterestPushEnabled(userId: String, pushEnabled: Boolean) {
        interestPushEnabledUpsert = InterestPushEnabledUpsertCall(
            userId = userId,
            pushEnabled = pushEnabled,
        )
        interestPushEnabledLatch.countDown()
    }

    override suspend fun touchLastActive(userId: String) = Unit

    fun awaitInterestProfileUpsert(timeoutSeconds: Long = 5): InterestUpsertCall {
        assertTrue("interest profile upsert timed out", interestLatch.await(timeoutSeconds, TimeUnit.SECONDS))
        return checkNotNull(interestUpsert)
    }

    fun awaitSubscriptionUpsert(timeoutSeconds: Long = 5): SubscriptionUpsertCall {
        assertTrue("subscription upsert timed out", subscriptionLatch.await(timeoutSeconds, TimeUnit.SECONDS))
        return checkNotNull(subscriptionUpsert)
    }

    fun awaitInterestPushEnabledUpsert(timeoutSeconds: Long = 5): InterestPushEnabledUpsertCall {
        assertTrue(
            "interest push_enabled upsert timed out",
            interestPushEnabledLatch.await(timeoutSeconds, TimeUnit.SECONDS)
        )
        return checkNotNull(interestPushEnabledUpsert)
    }
}

private class FakeSettingRepository(initialPushOptIn: Boolean) : SettingRepository {
    private val darkTheme = MutableStateFlow(false)
    private val hasSeenOnboarding = MutableStateFlow(false)
    private val pushOptIn = MutableStateFlow(initialPushOptIn)

    override fun getDarkTheme(): Flow<Boolean> = darkTheme

    override fun getHasSeenOnboarding(): Flow<Boolean> = hasSeenOnboarding

    override fun getPushOptIn(): Flow<Boolean> = pushOptIn

    override suspend fun updateIsDarkTheme(isDarkTheme: Boolean) {
        darkTheme.value = isDarkTheme
    }

    override suspend fun updateHasSeenOnboarding(seen: Boolean) {
        hasSeenOnboarding.value = seen
    }

    override suspend fun updatePushOptIn(enabled: Boolean) {
        pushOptIn.value = enabled
    }
}

private class FakeAuthRepository(private val sessionState: SessionState) : AuthRepository {
    override suspend fun signInWithGoogle(
        idToken: String,
        rawNonce: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit,
    ) = Unit

    override suspend fun signOut() = Unit

    override fun getSessionFlow(): Flow<SessionState> = flowOf(sessionState)

    override suspend fun deleteAccount(
        userId: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit,
    ) = Unit

    override suspend fun getProfile(userId: String): UserProfile? = null

    override suspend fun updateProfile(
        userId: String,
        displayName: String,
        avatarUrl: String?,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit,
    ) = Unit

    override suspend fun uploadProfileAvatar(
        userId: String,
        bytes: ByteArray,
        contentType: String,
    ): String = "https://example.com/avatar.jpg"
}
