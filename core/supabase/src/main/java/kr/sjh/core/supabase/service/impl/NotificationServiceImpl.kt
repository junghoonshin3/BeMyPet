package kr.sjh.core.supabase.service.impl

import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from
import kr.sjh.core.model.notification.UserInterestProfile
import kr.sjh.core.supabase.service.NotificationService
import java.time.OffsetDateTime
import javax.inject.Inject

class NotificationServiceImpl @Inject constructor(
    postgrest: Postgrest,
) : NotificationService {

    private val interestTable = postgrest.from("user_interest_profiles")
    private val subscriptionTable = postgrest.from("notification_subscriptions")

    override suspend fun upsertInterestProfile(profile: UserInterestProfile) {
        interestTable.upsert(profile)
    }

    override suspend fun upsertSubscription(
        userId: String,
        fcmToken: String,
        pushOptIn: Boolean,
        timezone: String,
    ) {
        val payload = buildSubscriptionPayloadForTest(
            userId = userId,
            fcmToken = fcmToken,
            timezone = timezone,
        ) + mapOf("push_opt_in" to pushOptIn)

        subscriptionTable.upsert(payload)
    }

    override suspend fun touchLastActive(userId: String) {
        val normalizedUserId = userId.trim()
        if (normalizedUserId.isBlank()) return

        subscriptionTable.update(
            mapOf("last_active_at" to OffsetDateTime.now().toString())
        ) {
            filter {
                eq("user_id", normalizedUserId)
            }
        }
    }

    internal companion object {
        fun buildSubscriptionPayloadForTest(
            userId: String,
            fcmToken: String,
            timezone: String,
        ): Map<String, Any> {
            return mapOf(
                "user_id" to userId.trim(),
                "fcm_token" to fcmToken.trim(),
                "timezone" to timezone.trim().ifBlank { "Asia/Seoul" },
            )
        }
    }
}
