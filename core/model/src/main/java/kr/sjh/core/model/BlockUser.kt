package kr.sjh.core.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class BlockUser(
    @SerialName("blocker_id") val blockerUser: String,
    @SerialName("blocked_id") val blockedUser: String,
    @SerialName("raw_user_meta_data") val rawUserMetaData: JsonObject,
    @SerialName("created_at") val createdAt: String? = null,
)