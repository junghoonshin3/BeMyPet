package kr.sjh.data.repository.impl

import kr.sjh.core.model.notification.UserInterestProfile
import kr.sjh.core.supabase.service.NotificationService
import kr.sjh.data.repository.NotificationRepository
import javax.inject.Inject

class NotificationRepositoryImpl @Inject constructor(
    private val notificationService: NotificationService,
) : NotificationRepository {

    override suspend fun upsertInterestProfile(
        userId: String,
        regions: List<String>,
        species: List<String>,
        sexes: List<String>,
        sizes: List<String>,
        pushEnabled: Boolean,
    ) {
        val normalizedUserId = userId.trim()
        if (normalizedUserId.isBlank()) return

        notificationService.upsertInterestProfile(
            UserInterestProfile(
                userId = normalizedUserId,
                regions = regions.map { it.trim() }.filter { it.isNotBlank() }.distinct(),
                species = species.map { it.trim() }.filter { it.isNotBlank() }.distinct(),
                sexes = sexes.map { it.trim() }.filter { it.isNotBlank() }.distinct(),
                sizes = sizes.map { it.trim() }.filter { it.isNotBlank() }.distinct(),
                pushEnabled = pushEnabled,
            )
        )
    }

    override suspend fun upsertSubscription(
        userId: String,
        token: String,
        pushOptIn: Boolean,
        timezone: String,
    ) {
        val normalizedUserId = userId.trim()
        val normalizedToken = token.trim()
        if (normalizedUserId.isBlank() || normalizedToken.isBlank()) return

        notificationService.upsertSubscription(
            userId = normalizedUserId,
            fcmToken = normalizedToken,
            pushOptIn = pushOptIn,
            timezone = timezone,
        )
    }

    override suspend fun touchLastActive(userId: String) {
        notificationService.touchLastActive(userId)
    }
}
