package kr.sjh.core.supabase.service

import kr.sjh.core.model.notification.UserInterestProfile

interface NotificationService {
    suspend fun upsertInterestProfile(profile: UserInterestProfile)

    suspend fun upsertSubscription(
        userId: String,
        fcmToken: String,
        pushOptIn: Boolean,
        timezone: String,
    )

    suspend fun touchLastActive(userId: String)
}
