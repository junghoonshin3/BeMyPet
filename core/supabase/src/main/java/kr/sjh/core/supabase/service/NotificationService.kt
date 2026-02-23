package kr.sjh.core.supabase.service

import kr.sjh.core.model.notification.UserInterestProfile

interface NotificationService {
    suspend fun upsertInterestProfile(profile: UserInterestProfile)

    suspend fun getInterestProfile(userId: String): UserInterestProfile?

    suspend fun upsertInterestPushEnabled(
        userId: String,
        pushEnabled: Boolean,
    )

    suspend fun upsertSubscription(
        userId: String,
        fcmToken: String,
        pushOptIn: Boolean,
        timezone: String,
    )

    suspend fun touchLastActive(userId: String)
}
