package kr.sjh.data.repository

import kr.sjh.core.model.notification.UserInterestProfile

interface NotificationRepository {
    suspend fun upsertInterestProfile(
        userId: String,
        regions: List<String>,
        species: List<String>,
        sexes: List<String>,
        sizes: List<String>,
        pushEnabled: Boolean,
    )

    suspend fun getInterestProfile(userId: String): UserInterestProfile?

    suspend fun upsertSubscription(
        userId: String,
        token: String,
        pushOptIn: Boolean,
        timezone: String,
    )

    suspend fun touchLastActive(userId: String)
}
