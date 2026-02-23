package kr.sjh.core.supabase.service.impl

import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
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

    override suspend fun getInterestProfile(userId: String): UserInterestProfile? {
        val normalizedUserId = userId.trim()
        if (normalizedUserId.isBlank()) return null

        return interestTable.select {
            filter {
                eq("user_id", normalizedUserId)
            }
        }.decodeList<UserInterestProfile>().firstOrNull()
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
            pushOptIn = pushOptIn,
            timezone = timezone,
        )

        subscriptionTable.upsert(payload) {
            onConflict = "fcm_token"
        }
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
            pushOptIn: Boolean,
            timezone: String,
        ): NotificationSubscriptionUpsertPayload {
            return NotificationSubscriptionUpsertPayload(
                userId = userId.trim(),
                fcmToken = fcmToken.trim(),
                pushOptIn = pushOptIn,
                timezone = timezone.trim().ifBlank { "Asia/Seoul" },
            )
        }
    }
}

@Serializable
internal data class NotificationSubscriptionUpsertPayload(
    @SerialName("user_id") val userId: String,
    @SerialName("fcm_token") val fcmToken: String,
    @SerialName("push_opt_in") val pushOptIn: Boolean,
    @SerialName("timezone") val timezone: String,
)
