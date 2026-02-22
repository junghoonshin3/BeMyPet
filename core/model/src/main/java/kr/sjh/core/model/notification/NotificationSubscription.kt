package kr.sjh.core.model.notification

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NotificationSubscription(
    @SerialName("id") val id: String? = null,
    @SerialName("user_id") val userId: String,
    @SerialName("fcm_token") val fcmToken: String,
    @SerialName("push_opt_in") val pushOptIn: Boolean = true,
    @SerialName("last_active_at") val lastActiveAt: String? = null,
    @SerialName("last_sent_at") val lastSentAt: String? = null,
    @SerialName("daily_sent_count") val dailySentCount: Int = 0,
    @SerialName("timezone") val timezone: String = "Asia/Seoul",
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
)
