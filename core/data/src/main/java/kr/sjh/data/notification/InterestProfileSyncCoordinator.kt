package kr.sjh.data.notification

import kotlinx.coroutines.flow.first
import kr.sjh.core.model.notification.UserInterestProfile
import kr.sjh.data.repository.FavouriteRepository
import kr.sjh.data.repository.NotificationRepository
import kr.sjh.data.repository.SettingRepository
import javax.inject.Inject

class InterestProfileSyncCoordinator @Inject constructor(
    private val favouriteRepository: FavouriteRepository,
    private val notificationRepository: NotificationRepository,
    private val settingRepository: SettingRepository,
) {

    suspend fun syncFromFavorites(userId: String) {
        val normalizedUserId = userId.trim()
        if (normalizedUserId.isBlank()) return

        val favourites = favouriteRepository.getFavouritePets().first()
        val derived = FavoriteInterestProfileDeriver.derive(favourites)
        val existing = notificationRepository.getInterestProfile(normalizedUserId)
        val pushEnabled = settingRepository.getPushOptIn().first()

        val merged = mergeProfile(existing, derived, normalizedUserId, pushEnabled)

        notificationRepository.upsertInterestProfile(
            userId = merged.userId,
            regions = merged.regions,
            species = merged.species,
            sexes = merged.sexes,
            sizes = merged.sizes,
            pushEnabled = merged.pushEnabled,
        )
    }

    internal fun mergeProfile(
        existing: UserInterestProfile?,
        derived: DerivedInterestProfile,
        userId: String,
        pushEnabled: Boolean,
    ): UserInterestProfile {
        return UserInterestProfile(
            userId = userId,
            regions = union(existing?.regions.orEmpty(), derived.regions),
            species = union(existing?.species.orEmpty(), derived.species),
            sexes = union(existing?.sexes.orEmpty(), derived.sexes),
            sizes = union(existing?.sizes.orEmpty(), derived.sizes),
            pushEnabled = pushEnabled,
        )
    }

    private fun union(existing: List<String>, derived: List<String>): List<String> {
        return (existing + derived)
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .distinct()
            .sorted()
    }
}
