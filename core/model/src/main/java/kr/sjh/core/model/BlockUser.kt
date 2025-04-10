package kr.sjh.core.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BlockUser(
    @SerialName("blocker_id") val blockerUser: String,
    @SerialName("blocked_id") val blockedUser: String,
    @SerialName("created_at") val createdAt: String? = null,
)