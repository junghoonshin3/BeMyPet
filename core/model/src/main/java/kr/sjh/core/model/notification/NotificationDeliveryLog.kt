package kr.sjh.core.model.notification

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class NotificationDeliveryLog(
    @SerialName("id") val id: String? = null,
    @SerialName("user_id") val userId: String,
    @SerialName("campaign_type") val campaignType: String,
    @SerialName("notice_no") val noticeNo: String? = null,
    @SerialName("dedupe_key") val dedupeKey: String,
    @SerialName("status") val status: String,
    @SerialName("payload_json") val payloadJson: JsonObject? = null,
    @SerialName("sent_at") val sentAt: String? = null,
    @SerialName("opened_at") val openedAt: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
)
