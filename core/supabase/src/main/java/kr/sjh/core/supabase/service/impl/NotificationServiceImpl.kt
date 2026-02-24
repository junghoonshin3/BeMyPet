package kr.sjh.core.supabase.service.impl

import android.util.Log
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.exceptions.RestException
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kr.sjh.core.model.notification.UserInterestProfile
import kr.sjh.core.supabase.service.NotificationService
import java.time.OffsetDateTime
import javax.inject.Inject

class NotificationServiceImpl @Inject constructor(
    private val postgrest: Postgrest,
    private val auth: Auth,
) : NotificationService {

    private val interestTable = postgrest.from("user_interest_profiles")
    private val subscriptionTable = postgrest.from("notification_subscriptions")

    override suspend fun upsertInterestProfile(profile: UserInterestProfile) {
        withAuthRefreshRetry("upsertInterestProfile") {
            interestTable.upsert(profile)
        }
    }

    override suspend fun upsertInterestPushEnabled(
        userId: String,
        pushEnabled: Boolean,
    ) {
        val payload = buildInterestPushPayloadForTest(
            userId = userId,
            pushEnabled = pushEnabled,
        )

        withAuthRefreshRetry("upsertInterestPushEnabled") {
            interestTable.upsert(payload) {
                onConflict = "user_id"
            }
        }
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
        val normalizedUserId = userId.trim()
        if (normalizedUserId.isBlank()) return

        val payload = buildSubscriptionRpcPayloadForTest(
            fcmToken = fcmToken,
            pushOptIn = pushOptIn,
            timezone = timezone,
        )

        withAuthRefreshRetry("upsertSubscription") {
            postgrest.rpc(
                function = UPSERT_SUBSCRIPTION_RPC,
                parameters = buildJsonObject {
                    put("p_fcm_token", payload.fcmToken)
                    put("p_push_opt_in", payload.pushOptIn)
                    put("p_timezone", payload.timezone)
                }
            )
        }
    }

    override suspend fun touchLastActive(userId: String) {
        val normalizedUserId = userId.trim()
        if (normalizedUserId.isBlank()) return

        withAuthRefreshRetry("touchLastActive") {
            subscriptionTable.update(
                mapOf("last_active_at" to OffsetDateTime.now().toString())
            ) {
                filter {
                    eq("user_id", normalizedUserId)
                }
            }
        }
    }

    private suspend fun <T> withAuthRefreshRetry(operation: String, action: suspend () -> T): T {
        return try {
            action()
        } catch (firstError: Throwable) {
            if (!shouldRetryAfterAuthRefresh(firstError)) {
                throw firstError
            }

            Log.w(
                TAG,
                "$operation failed with expired auth. refreshing session and retrying once."
            )

            val refreshAttempt = runCatching { auth.refreshCurrentSession() }
            if (refreshAttempt.isFailure) {
                refreshAttempt.exceptionOrNull()?.let { firstError.addSuppressed(it) }
                throw firstError
            }

            action()
        }
    }

    private fun shouldRetryAfterAuthRefresh(throwable: Throwable): Boolean {
        val rest = throwable as? RestException
        return shouldRetryAfterAuthRefreshForTest(
            statusCode = rest?.statusCode,
            message = throwable.message,
            errorPayload = rest?.error,
        )
    }

    internal companion object {
        private const val TAG = "NotificationService"
        private const val UPSERT_SUBSCRIPTION_RPC = "upsert_my_notification_subscription"

        internal fun shouldRetryAfterAuthRefreshForTest(
            statusCode: Int?,
            message: String?,
            errorPayload: String?,
        ): Boolean {
            if (statusCode == 401) return true

            val merged = buildString {
                append(message.orEmpty())
                append(' ')
                append(errorPayload.orEmpty())
            }.lowercase()

            return merged.contains("jwt expired") || merged.contains("invalid jwt")
        }

        fun buildSubscriptionRpcPayloadForTest(
            fcmToken: String,
            pushOptIn: Boolean,
            timezone: String,
        ): NotificationSubscriptionRpcPayload {
            return NotificationSubscriptionRpcPayload(
                fcmToken = fcmToken.trim(),
                pushOptIn = pushOptIn,
                timezone = timezone.trim().ifBlank { "Asia/Seoul" },
            )
        }

        fun buildInterestPushPayloadForTest(
            userId: String,
            pushEnabled: Boolean,
        ): NotificationInterestPushUpsertPayload {
            return NotificationInterestPushUpsertPayload(
                userId = userId.trim(),
                pushEnabled = pushEnabled,
            )
        }
    }
}

@Serializable
internal data class NotificationInterestPushUpsertPayload(
    @SerialName("user_id") val userId: String,
    @SerialName("push_enabled") val pushEnabled: Boolean,
)

@Serializable
internal data class NotificationSubscriptionRpcPayload(
    @SerialName("fcm_token") val fcmToken: String,
    @SerialName("push_opt_in") val pushOptIn: Boolean,
    @SerialName("timezone") val timezone: String,
)
