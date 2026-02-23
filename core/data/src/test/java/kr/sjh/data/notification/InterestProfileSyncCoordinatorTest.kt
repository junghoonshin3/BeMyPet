package kr.sjh.data.notification

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kr.sjh.core.model.adoption.Pet
import kr.sjh.core.model.notification.UserInterestProfile
import kr.sjh.data.repository.FavouriteRepository
import kr.sjh.data.repository.NotificationRepository
import kr.sjh.data.repository.SettingRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class InterestProfileSyncCoordinatorTest {

    @Test
    fun syncFromFavorites_merges_existing_and_derived_interests_with_union() = runBlocking {
        val fakeFavoriteRepository = FakeFavouriteRepository(
            pets = listOf(
                Pet(noticeNo = "전남-함평-2026-00071", upKindCode = "417000", sex = "M", weight = "4kg"),
            )
        )
        val fakeNotificationRepository = FakeNotificationRepository(
            existing = UserInterestProfile(
                userId = "user-1",
                regions = listOf("6110000"),
                species = listOf("cat"),
                sexes = listOf("F"),
                sizes = listOf("MEDIUM"),
                pushEnabled = false,
            )
        )
        val coordinator = InterestProfileSyncCoordinator(
            favouriteRepository = fakeFavoriteRepository,
            notificationRepository = fakeNotificationRepository,
            settingRepository = FakeSettingRepository(pushOptIn = true),
        )

        coordinator.syncFromFavorites(" user-1 ")

        assertEquals("user-1", fakeNotificationRepository.lastUpsertUserId)
        assertEquals(listOf("6110000", "6460000"), fakeNotificationRepository.lastRegions)
        assertEquals(listOf("cat", "dog"), fakeNotificationRepository.lastSpecies)
        assertEquals(listOf("F", "M"), fakeNotificationRepository.lastSexes)
        assertEquals(listOf("MEDIUM", "SMALL"), fakeNotificationRepository.lastSizes)
        assertEquals(true, fakeNotificationRepository.lastPushEnabled)
    }

    @Test
    fun syncFromFavorites_skips_when_user_id_blank() = runBlocking {
        val fakeNotificationRepository = FakeNotificationRepository(existing = null)
        val coordinator = InterestProfileSyncCoordinator(
            favouriteRepository = FakeFavouriteRepository(pets = emptyList()),
            notificationRepository = fakeNotificationRepository,
            settingRepository = FakeSettingRepository(pushOptIn = true),
        )

        coordinator.syncFromFavorites("   ")

        assertNull(fakeNotificationRepository.lastUpsertUserId)
    }
}

private class FakeFavouriteRepository(
    private val pets: List<Pet>,
) : FavouriteRepository {
    override fun isExist(desertionNo: String): Boolean = false

    override suspend fun addPet(pet: Pet) = Unit

    override suspend fun removePet(desertionNo: String) = Unit

    override fun getFavouritePets(): Flow<List<Pet>> = flowOf(pets)

    override suspend fun backfillFavouriteImagesIfNeeded() = Unit
}

private class FakeNotificationRepository(
    private val existing: UserInterestProfile?,
) : NotificationRepository {
    var lastUpsertUserId: String? = null
    var lastRegions: List<String> = emptyList()
    var lastSpecies: List<String> = emptyList()
    var lastSexes: List<String> = emptyList()
    var lastSizes: List<String> = emptyList()
    var lastPushEnabled: Boolean? = null

    override suspend fun upsertInterestProfile(
        userId: String,
        regions: List<String>,
        species: List<String>,
        sexes: List<String>,
        sizes: List<String>,
        pushEnabled: Boolean,
    ) {
        lastUpsertUserId = userId
        lastRegions = regions
        lastSpecies = species
        lastSexes = sexes
        lastSizes = sizes
        lastPushEnabled = pushEnabled
    }

    override suspend fun getInterestProfile(userId: String): UserInterestProfile? = existing

    override suspend fun upsertSubscription(
        userId: String,
        token: String,
        pushOptIn: Boolean,
        timezone: String,
    ) = Unit

    override suspend fun touchLastActive(userId: String) = Unit
}

private class FakeSettingRepository(
    private val pushOptIn: Boolean,
) : SettingRepository {
    override fun getDarkTheme(): Flow<Boolean> = flowOf(false)

    override fun getHasSeenOnboarding(): Flow<Boolean> = flowOf(false)

    override fun getPushOptIn(): Flow<Boolean> = flowOf(pushOptIn)

    override suspend fun updateIsDarkTheme(isDarkTheme: Boolean) = Unit

    override suspend fun updateHasSeenOnboarding(seen: Boolean) = Unit

    override suspend fun updatePushOptIn(enabled: Boolean) = Unit
}
