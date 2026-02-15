package kr.sjh.core.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BlockUser(
    @SerialName("blocker_id") val blockerUser: String,
    @SerialName("blocked_id") val blockedUser: String,
    @SerialName("blocked_name") val blockedName: String? = null,
    @SerialName("blocked_avatar_url") val blockedAvatarUrl: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
)
